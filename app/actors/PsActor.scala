package actors

import java.sql.Timestamp
import java.util.Date
import javax.inject.Inject

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import models.daos.{GameDAO, OfferDAO, PsStoreDAO}
import models.entities.{Offer, PsStore}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws.WSClient

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import org.joda.time.DateTime

object PsActor {
    case class Query(game: Long)
    case object Update
    def props(gameDAO: GameDAO, offerDAO: OfferDAO, psDAO: PsStoreDAO)(implicit ws:WSClient) =
        Props(new PsActor(gameDAO, offerDAO, psDAO)(ws))
}

class PsActor @Inject() (gameDAO: GameDAO, offerDAO: OfferDAO, psDAO: PsStoreDAO)
                        (implicit ws: WSClient) extends Actor{
    import context._
    import actors.PsActor._
    // https://store.playstation.com/store/api/chihiro/00_09_000/tumbler/CL/es/999/<query>?suggested_size=5&mode=game
    // https://store.playstation.com/chihiro-api/viewfinder/CL/es/999/STORE-MSF77008-ALLDEALS?game_content_type=games&platform=ps4%2Cps3&size=30&gkb=1&geoCountry=CL
    /*
        ...
        links = [
            {
                default_sku = {
                    display_price = "US$22.99"
                    price = 2299
                    rewards = [
                        {
                            discount = 50
                            price = 1149
                            display_price = "US$11.49"
                            isPlus = False
                            start_date = "2016-06-07T15:00:00Z"
                            end_date = "2016-06-13T15:00:00Z"
                        }
                    ]
                    ...
                }
                metadata = {
                    genre, game_genre, game_subgenre, playable_platform, ...
                }
                name = "Resident Evil 4"
                playable_platform = [ PS3 ]
                release_date = "2011-09-20T00:00:00Z"
                url = "bla"
        ]

     */
    case class OfferData(id: String, platforms: Seq[String], url: String,
                         base_price: Double, discounted_price: Double,
                         start_date: String, end_date: String)

    implicit val offerRead: Reads[OfferData] = (
        (__ \ "id").read[String] and
        (__ \ "playable_platform").read[Seq[String]] and
        (__ \ "url").read[String] and
        (__ \ "default_sku" \ "price").read[Double] and
        ((__ \ "default_sku" \ "rewards")(0) \ "price").read[Double] and
        ((__ \ "default_sku" \ "rewards")(0) \ "start_date").read[String] and
        ((__ \ "default_sku" \ "rewards")(0) \ "end_date").read[String])(OfferData.apply _)

    implicit def  strToTimestamp(s: String): Timestamp = {
        // start_date = "2016-06-07T15:00:00Z"
        val date = (s split "T")(0) split "-"
        val time = (s split "T")(1) split ":"
        new Timestamp(new DateTime(date(0).toInt, date(1).toInt, date(2).toInt, time(0).toInt, time(1).toInt).getMillis)
    }

    val allDealsUrl = "https://store.playstation.com/chihiro-api/viewfinder/CL/es/999/STORE-MSF77008-ALLDEALS?game_content_type=games&platform=ps4%2Cps3&size=100&gkb=1&geoCountry=CL"
    val baseUrl = "https://store.playstation.com/#!/es-cl/juegos/cid="
    def receive = {
        case Update => {
            println("PsActor: Message received from ", sender)
            val s = sender
            psDAO.all.map{ seq =>
                val gameIDs: Map[String, Long] = seq.toMap
                println("PsActor: Getting PsStore's JSON response")
                val response = ws.url(allDealsUrl).get().map {
                    resp => (resp.json \ "links").as[List[OfferData]]
                }.onComplete{
                    case Success(newOffers) =>
                        val number = processOffers(newOffers, gameIDs)
                        println("PsActor: Sending response to UpdateActor", sender, sender.path)
                        s ! ("Se encontraron " + number + " ofertas en PsStore")
                    case Failure(error) =>
                        s ! "Error al buscar ofertas en PsStore.\n" + error
                }
            }
        }
        case a => println("Recibí " + a)
    }

    def processOffers(newOffers: List[OfferData], gameIDs: Map[String, Long]) = {
        println("PsActor: Processing " + newOffers.length + " new offers.")
        val validOffers =
            for {
                offer <- newOffers
                if gameIDs contains offer.id
                playable_platform <- offer.platforms
            } yield Offer(0,
                baseUrl + offer.id,
                gameIDs.get(offer.id) match {
                    case Some(g) => g
                    case None => 0
                },
                playable_platform match {
                    case "PS3" => 2
                    case _ => 1         // "PS4 TM"
                },
                1,
                offer.start_date,
                offer.end_date,
                offer.base_price/100.0,
                offer.discounted_price/100.0)
        println("PsActor: Found " + validOffers.length + " offers")
        val rows: Seq[Long] = Await.result(offerDAO.insert(validOffers), Timeout(1 minute).duration)
        println("PsActor: Created " + rows.length + " offers")
        println("PsActor: JSON response processed.")
        rows.length
    }

}