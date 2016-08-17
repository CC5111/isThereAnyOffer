package actors


import akka.actor.{Actor, Props}
import akka.util.Timeout
import models.daos.{OfferDAO, SteamDAO}
import models.entities.Offer
import scraping.SteamScraping
import scraping.DataSteamDb

import scala.concurrent.Await
import scala.concurrent.duration._

object SteamActor {
    case class Query(game: String)
    case object Update
    def props(offerDAO: OfferDAO, steamDAO: SteamDAO) =
        Props(new SteamActor(offerDAO, steamDAO))
}

class SteamActor (offerDAO: OfferDAO, steamDAO: SteamDAO) extends Actor{
    import context._
    import actors.SteamActor._

    val dealsURL = "https://steamdb.info/sales/?merged=true"
    private val crawler = SteamScraping(dealsURL)

    def receive = {
        case Update => {
            println("SteamActor: Message received from " + sender)
            val s = sender
            steamDAO.all.map{ seq =>
                val gameIDs: Map[String, Long] = seq.toMap
                println("SteamActor: Scraping SteamDB")
                val newOffers = crawler.offersWithDiscount()
                val number = processOffers(newOffers, gameIDs)
                println("SteamActor: Sending response to UpdateActor (" + s + ")")
                s ! ("Se encontraron " + number + " ofertas en Steam")
            }
        }
        case a => println("SteamActor: Couldn't understand message " + a  +".")
    }

    def processOffers(newOffers: List[DataSteamDb], gameIDs: Map[String, Long]) = {
        println("SteamActor: Processing " + newOffers.length + " offers.")
        val validOffers =
            for {
                offer <- newOffers
                if gameIDs contains offer.id
            } yield Offer(0,
                "https://store.steampowered.com/app/" + offer.id,
                gameIDs.get(offer.id) match {
                    case Some(g) => g
                    case None => 0
                },
                5,  // PC
                3,  // Steam
                offer.fromDate,
                offer.untilDate,
                offer.normalPrice,
                offer.offerPrice,
                (100 - (offer.offerPrice/offer.normalPrice*100)).toInt,
                0)
        println("SteamActor: Found " + validOffers.length + " valid offers")
        val newAndOldOffers = validOffers.map(validOffer => {
            Await.result(offerDAO.insertIfNotExists(validOffer), Timeout(10 seconds).duration)
        }).partition(_.nonEmpty)
        println("SteamActor: Created " + newAndOldOffers._1.length + " new offers")
        println("SteamActor: " + newAndOldOffers._2.length + " offers already existed")
        println("SteamActor: response processed.")
        newAndOldOffers._1.length
    }
}
