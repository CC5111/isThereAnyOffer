package actors


import java.sql.Timestamp
import java.util.Date

import akka.actor.{Actor, ActorRef, Props}
import models.daos.{GameDAO, OfferDAO}
import models.entities.Offer
import play.api.libs.json._
import play.api.libs.functional.syntax._

object GogActor {
    case class Query(game: Long)
    case class Update()
    def props(gameDAO: GameDAO, offerDAO: OfferDAO) =
        Props(classOf[GogActor], gameDAO, offerDAO)
}

class GogActor(gameDAO: GameDAO, offerDAO: OfferDAO) extends Actor{
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

    def receive = {
        case Query(game) => {
            ???
        }
        case Update() => {
            val response: JsValue = ???
            val game_ids: Map[String, Long] = ???

            val new_offers = (response \ "products").as[List[OfferData]]
            val valid_offers =
                for {
                    offer <- new_offers
                    if game_ids contains offer.id.toString
                } yield Offer(0,
                    "www.gog.com" + offer.url,
                    game_ids.get(offer.id.toString) match {
                        case Some(g) => g
                        case None => 0
                    },
                    5,  // PC
                    "GOG",
                    new Timestamp(offer.start_date*1000),
                    new Timestamp(offer.end_date*1000),
                    offer.base_price,
                    offer.discounted_price)
            offerDAO.insert(valid_offers)
        }
    }

}