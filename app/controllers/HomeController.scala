package controllers

import javax.inject.Inject

import actors.SearchActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import models.daos.{GameDAO, GenreDAO, OfferDAO, PlatformDAO}
import models.entities.{Game, Genre, Offer}
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, AnyContent, Controller, WebSocket}

import scala.collection.mutable
import scala.concurrent.ExecutionContext

class HomeController @Inject()(gameDAO: GameDAO,
                               offerDAO: OfferDAO,
                               platformDAO: PlatformDAO,
                               genreDAO: GenreDAO)(implicit ec:ExecutionContext, system: ActorSystem, mat:Materializer) extends Controller {


  def index() = Action.async { implicit request =>
    for {
      tuplesPlatformCount <- platformDAO.allPlatformsOffersWithCount
      tuplesGenreCount <- genreDAO.allGenresWithCount
      tuplesOfferGamePlatform <- offerDAO.actualOffers  //obtencion de las ofertas
      tuplesBestOfferGamePlatform <- offerDAO.lastGamesWithOffers
      gamesWithGenres <- genreDAO.allGamesWithGenres()
    } yield Ok(views.html.home(
      title = "Inicio",
      tuplesPlatformCount = tuplesPlatformCount.toList,
      tuplesGenreCount = tuplesGenreCount.toList,
      tuplesOfferGamePlatform = tuplesOfferGamePlatform.toList,
      tuplesBestOfferGamePlatform = tuplesBestOfferGamePlatform.toList,
      hashTableGenres = gamesWithGenres.groupBy(_._1).map{case (k, v) => (k, v.map(_._2).toList)}
    ))
  }

  def search() = Action(parse.urlFormEncoded) { request =>
    Redirect(routes.ResultController.index(request.body.get("search").head.head))
  }

  // JsValue ~ JSON
  def socket = WebSocket.accept[JsValue, JsValue] { request =>
    ActorFlow.actorRef(out => SearchActor.props(out,gameDAO))
  }
}
