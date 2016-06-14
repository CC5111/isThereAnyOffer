package actors

import java.sql.Timestamp
import java.util.Date

import akka.actor.{Actor, ActorRef, Props}
import models.daos.{GameDAO, OfferDAO}
import models.entities.Offer
import play.api.libs.json._
import play.api.libs.functional.syntax._

object PsActor {
    case class Query(game: Long)
    case class Update()
    def props(gameDAO: GameDAO, offerDAO: OfferDAO) =
        Props(classOf[SteamActor], gameDAO, offerDAO)
}

class PsActor(gameDAO: GameDAO, offerDAO: OfferDAO) extends Actor{
    import context._
    import actors.PsActor._
    // https://store.playstation.com/store/api/chihiro/00_09_000/tumbler/CL/es/999/<query>?suggested_size=5&mode=game
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
    case class OfferData(id: String, platform: String, url: String, base_price: Double, discounted_price: Double, start_date: String, end_date: String)
    implicit val offerRead: Reads[OfferData] = (
        (__ \ "id").read[String] and
        (__ \ "playable_platform")(0).read[String] and
        (__ \ "url").read[String] and
        (__ \ "default_sku" \ "price").read[Double] and
        ((__ \ "default_sku" \ "rewards")(0) \ "price").read[Double] and
        ((__ \ "default_sku" \ "rewards")(0) \ "start_date").read[String] and
        ((__ \ "default_sku" \ "rewards")(0) \ "end_date").read[String])(OfferData.apply _)

    implicit def  strToTimestamp(s: String): Timestamp = {
        // start_date = "2016-06-07T15:00:00Z"
        val date = (s split "T")(0)
        val split_date = date split "-"
        new Timestamp(new Date(split_date(0).toInt, split_date(1).toInt, split_date(2).toInt).getTime)
    }

    def receive = {
        case Query(game) => {
            ???
        }
        case Update() => {
            /*val response: JsValue = ws.url(url).get().map {
                resp => resp.json
            }*/
            val response: JsObject = ???
            val game_ids: Map[String, Long] = ???

            val new_offers = (response \ "links").as[List[OfferData]]
            val valid_offers =
                for {
                    offer <- new_offers
                    if game_ids contains offer.id
                } yield Offer(0,
                    offer.url,
                    game_ids.get(offer.id) match {
                        case Some(g) => g
                        case None => 0
                    },
                    offer.platform match {
                        case "PS3" => 1
                        case _ => 2         // "PS4 TM"
                    },
                    "PlayStation Store",
                    offer.start_date,
                    offer.end_date,
                    offer.base_price/100.0,
                    offer.discounted_price/100.0)
            offerDAO.insert(valid_offers)
        }
    }


}