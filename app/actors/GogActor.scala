package actors


import java.sql.Timestamp
import javax.inject._
import java.util.Date

import akka.actor.{Actor, ActorRef, Props}
import models.daos.{GameDAO, OfferDAO}
import models.entities.Offer
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws.WSClient

import scala.util.{Failure, Success}

object GogActor {
    case class Query(game: Long)
    case class Update()
    def props(gameDAO: GameDAO, offerDAO: OfferDAO)(implicit ws:WSClient) =
        Props(new GogActor(gameDAO, offerDAO)(ws))
}

class GogActor @Inject() (gameDAO: GameDAO, offerDAO: OfferDAO)
                         (implicit ws: WSClient) extends Actor{
    import context._
    import actors.GogActor._

    // https://www.gog.com/games/ajax/filtered?mediaType=game&price=discounted&page=1
    /*
        products = [
            {
                title = "Witcher 3"
                buyable = true
                image = "url"
                id = 1209819203
                url = "/game/the_witcher_3_wild_hunt"
                price = {
                    amount = "12.49"
                    baseAmount = "24.99"
                    finalAmount = "12.49"
                    isDiscounted = True
                    discountPercentage = 50
                    discountDifference = "12.50"
                    ...
                }
                isDiscounted = True
                salesVisibility = {
                    isActive = True
                    fromObject = {
                        date = "2015-05-19 02:00:00"
                        ...
                    }
                    toObject = {
                        date = "2020-12-31 02:00:00"
                        ...
                    }
                }
                category = "Role-Playing"
                isGame = True
                ...
            }, ...
        ]
        totalGamesFound = n

     */

    case class OfferData(id: Long, url: String, base_price: Double, discounted_price: Double, start_date: Long, end_date: Long)
    implicit val offerRead: Reads[OfferData] = (
        (__ \ "id").read[Long] and
        (__ \ "url").read[String] and
        (__ \ "price" \ "baseAmount").read[Double] and
        (__ \ "price" \ "finalAmount").read[Double] and
        (__ \ "salesVisibility" \ "from").read[Long] and
        (__ \ "salesVisibility" \ "to").read[Long])(OfferData.apply _)
    implicit def longToTimestamp(l: Long): Timestamp = {
        new Timestamp(l*1000)
    }

    def receive = {
        case Update() => {
            //val gameIDs: Map[String, Long] = ???
            println("GOGActor: Message received")
            val gameIDs: Map[String, Long] = Map[String, Long](("id1", 1), ("id2", 2))
            println("GOGActor: Getting GOG's JSON response")
            val response =  ws.url("https://www.gog.com/games/ajax/filtered?mediaType=game&price=discounted&page=1").get().map {
                resp => (resp.json \ "products").as[List[OfferData]]
            }.onComplete{
                case Success(newOffers) =>
                    println("GOGActor: Sending response to UpdateActor")
                    sender() ! ("Se encontraron " + processOffers(newOffers, gameIDs) + " nuevas ofertas en GOG")
                case Failure(error) =>
                    sender() ! "Error al buscar ofertas en GOG"
            }
        }
    }

    def processOffers(newOffers: List[OfferData], gameIDs: Map[String, Long]) = {
        println("GOGActor: Processing GOG's JSON response")
        val validOffers =
            for {
                offer <- newOffers
                if gameIDs contains offer.id.toString
            } yield Offer(0,
                "www.gog.com" + offer.url,
                gameIDs.get(offer.id.toString) match {
                    case Some(id) => id
                    case None => 0
                },
                5,  // PC
                "GOG",
                offer.start_date,
                offer.end_date,
                offer.base_price,
                offer.discounted_price)
        //offerDAO.insert(valid_offers)
        //validOffers.length
        newOffers.length
    }

}