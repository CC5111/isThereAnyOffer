package controllers

import javax.inject.Inject

import actors.SearchActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import models.daos.{GameDAO, GenreDAO, OfferDAO, PlatformDAO}
import models.entities.{Game, Offer, Platform}
import play.api.libs.json._
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, Controller, WebSocket}

import scala.concurrent.ExecutionContext

class ApiController @Inject()(gameDAO: GameDAO, offerDAO: OfferDAO)
                             (implicit ec:ExecutionContext, system: ActorSystem, mat:Materializer) extends Controller {

    implicit val gameWrites = new Writes[Game] {
        def writes(game: Game) = Json.obj(
            "id" -> game.id,
            "name" -> game.name,
            "cover" -> game.cover,
            "publisher" -> game.publisher,
            "developer" -> game.developer,
            "link" -> game.link,
            "description" -> game.description,
            "rating" -> game.rating,
            "releaseDate" -> game.releaseDate.toString,
            "typeGame" -> game.typeGame
        )
    }

    implicit val offerWrites = new Writes[Offer] {
        def writes(offer: Offer) = Json.obj(
            "id" -> offer.id,
            "idGame" -> offer.idGame,
            "idPlatform" -> offer.idPlatform,
            "link" -> offer.link,
            "store" -> offer.store,
            "normalPrice" -> offer.normalPrice,
            "offerPrice" -> offer.offerPrice,
            "discount" -> offer.discount,
            "fromDate" -> offer.fromDate,
            "untilDate" -> offer.untilDate.toString
        )
    }

    implicit val platformWrites = new Writes[Platform] {
        def writes(platform: Platform) = Json.obj(
            "id" -> platform.id,
            "name" -> platform.name
        )
    }

    implicit val bla = new Writes[(Offer, Game, Platform)] {
        override def writes(tuple: (Offer, Game, Platform)) = Json.obj(
            "offer" -> tuple._1,
            "game" -> tuple._2,
            "platform" -> tuple._3
        )
    }

    def games() = Action.async { implicit request =>
        gameDAO.all.map { games =>
            if (games.isEmpty)
                Ok(createErrorJSON("No existen juegos"))
            else
                Ok(createSuccessJSON(Json.toJson(games)))
        }
    }

    def gameById(id : Long) = Action.async{


        gameDAO.findById(id).map{
            case Some(game) =>  {
                Ok(createSuccessJSON(Json.toJson(List(game))))
            }
            case  None => {
                Ok(createErrorJSON("No existe juego con id = "+id))
            }
        }
    }

    def gameByName(name : String) = Action.async{

        gameDAO.searchByName(name).map{ games =>
            if (games.isEmpty)
                Ok(createErrorJSON("No existen juegos"))
            else
                Ok(createSuccessJSON(Json.toJson(games)))
        }

    }

    def filterOffers(pageNumber: Int, platform: String) = Action.async{
        offerDAO.filterOffers(
            pageNumber = pageNumber,
            platformFilter = platform
        ).map{ offers =>
            if (offers.isEmpty) Ok(createErrorJSON("No existen ofertas"))
            else Ok(createSuccessJSON(Json.toJson(offers)))
        }
    }

    def createSuccessJSON(data : JsValue) = {
        Json.obj(
            "data" -> data,
            "status" -> "success"
        )
    }
    def createErrorJSON( message : String) = {
        Json.obj(
            "message" -> message,
            "status" -> "error"
        )
    }

    def createFailJSON(data: JsValue) = {
        Json.obj(
            "data" -> data,
            "status" -> "fail"
        )
    }
}
