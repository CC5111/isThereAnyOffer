package actors

import java.sql.Timestamp
import javax.inject._

import akka.actor.{Actor, Props}
import akka.util.Timeout
import models.daos.{OfferDAO, GogDAO}
import models.entities.Offer
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws.WSClient

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object GogActor {
    case class Query(game: Long)
    case object Update
    def props(offerDAO: OfferDAO, gogDAO: GogDAO)(implicit ws:WSClient) =
        Props(new GogActor(offerDAO, gogDAO)(ws))
}

class GogActor @Inject() (offerDAO: OfferDAO, gogDAO: GogDAO)
                         (implicit ws: WSClient) extends Actor{
    import context._
    import actors.GogActor._

    case class OfferData(id: Long, url: String, base_price: String, discounted_price: String, start_date: Long, end_date: Long)
    implicit val offerRead: Reads[OfferData] = (
        (__ \ "id").read[Long] and
        (__ \ "url").read[String] and
        (__ \ "price" \ "baseAmount").read[String] and
        (__ \ "price" \ "finalAmount").read[String] and
        (__ \ "salesVisibility" \ "from").read[Long] and
        (__ \ "salesVisibility" \ "to").read[Long])(OfferData.apply _)
    implicit def longToTimestamp(l: Long): Timestamp = {
        new Timestamp(l*1000)
    }

    val allDealsUrl = "https://www.gog.com/games/ajax/filtered?mediaType=game&price=discounted&page=1"

    def receive = {
        case Update => {
            println("GOGActor: Message received from ", sender)
            val s = sender
            gogDAO.all.map { seq =>
                val gameIDs: Map[String, Long] = seq.toMap
                println("GOGActor: Getting GOG's JSON response")
                val response = ws.url(allDealsUrl).get().map { resp =>
                    (resp.json \ "products").as[List[OfferData]]
                }.onComplete {
                    case Success(newOffers) =>
                        val number = processOffers(newOffers, gameIDs)
                        println("GogActor: Sending response to UpdateActor (" + s + ")")
                        s ! ("Se encontraron " + number + " ofertas en GOG")
                    case Failure(error) =>
                        s ! "Error al buscar ofertas en GOG.\n" + error
                }
            }
        }
        case a => println("GogActor: Couldn't understand message " + a  +".")
    }

    def processOffers(newOffers: List[OfferData], gameIDs: Map[String, Long]) = {
        println("GogActor: Processing " + newOffers.length + " offers.")
        val validOffers =
            for {
                offer <- newOffers
                if gameIDs contains offer.id.toString
            } yield Offer(0,
                "https://www.gog.com" + offer.url,
                gameIDs.get(offer.id.toString) match {
                    case Some(id) => id
                    case None => 0
                },
                5,  // PC
                4,  // Store ID
                offer.start_date,
                offer.end_date,
                offer.base_price.toDouble,
                offer.discounted_price.toDouble)
        println("GogActor: Found " + validOffers.length + " offers")
        val newAndOldOffers = validOffers.map(validOffer => {
            Await.result(offerDAO.insertIfNotExists(validOffer), Timeout(10 seconds).duration)
        }).partition(_.nonEmpty)
        println("GogActor: Created " + newAndOldOffers._1.length + " new offers")
        println("GogActor: " + newAndOldOffers._2.length + " offers already existed")
        println("GogActor: JSON response processed.")
        newAndOldOffers._1.length
    }

}