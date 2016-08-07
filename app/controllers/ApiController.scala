package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import models.daos.{GameDAO, OfferDAO}
import models.entities.{Game, Genre, Offer, Platform, Category}
import play.api.libs.json._
import play.api.mvc.{Action, Controller}

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

  implicit val OfferGamePlatformWrites = new Writes[(Offer, Game, Platform)] {
    override def writes(tuple: (Offer, Game, Platform)) = Json.obj(
      "offer" -> tuple._1,
      "game" -> tuple._2,
      "platform" -> tuple._3
    )
  }

  implicit val genreCountWrites = new Writes[(Genre, Int)] {
    override def writes(tuple: (Genre, Int)) = Json.obj(
      "name" -> tuple._1.name,
      "count" -> tuple._2
    )
  }

  implicit val platformCountWrites = new Writes[(Platform, Int)] {
    override def writes(tuple: (Platform, Int)) = Json.obj(
      "name" -> tuple._1.name,
      "count" -> tuple._2
    )
  }

  implicit val categoriesCountWrites = new Writes[(Category, Int)] {
    override def writes(tuple: (Category, Int)) = Json.obj(
      "name" -> tuple._1.name,
      "count" -> tuple._2
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

  def filterOffers(pageNumber: Int, platform: String, genre: String, category: String) = Action.async{
    val result = offerDAO.filterOffers(
      pageNumber = pageNumber,
      platformFilter = platform,
      genreFilter = genre,
      categoryFilter = category
    )

    for {
      offers <- result._1
      genresCount <- result._2
      platformCount <- result._3
      categoriesCount <- result._4
      totalOffers <- result._5
    } yield if (offers.isEmpty) Ok(createErrorJSON("No existen ofertas"))
            else Ok(createSuccessJSON(Json.obj(
              "offers" -> Json.toJson(offers),
              // contadores de genero, plataformas y categorias
              "genre" -> Json.toJson(genresCount),
              "platform" -> Json.toJson(platformCount),
              "category" -> Json.toJson(categoriesCount),
              "totalOffers" -> Json.toJson(totalOffers.length))))
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
