package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.Materializer
import models.daos._
import models.entities.{Game, Genre, Offer}
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.{Action, AnyContent, Controller, WebSocket}

import scala.concurrent.ExecutionContext

@Singleton
class HomeController @Inject()(gameDAO: GameDAO,
                               offerDAO: OfferDAO,
                               platformDAO: PlatformDAO,
                               genreDAO: GenreDAO,
                               categoryDAO: CategoryDAO)(implicit ec:ExecutionContext, system: ActorSystem, mat:Materializer) extends Controller {

  def index() = Action.async { implicit request =>
    for {
      tuplesGamesWithOffersByEnd <- offerDAO.gamesWithOffersByEnd
      tuplesGamesWithOffersLessTenThousand <- offerDAO.gamesWithOffersLessTenThousand
      tuplesGamesWithLastOffers <- offerDAO.lastGamesWithOffers
      tuplesGamesWithBestOffers <- offerDAO.gamesWithBestOffers
      tuplesGamesWithOffersMoreVisits <- offerDAO.gamesWithOffersMoreVisits
    } yield Ok(views.html.home(
      title = "Inicio",
      tuplesGamesWithOffersByEnd = tuplesGamesWithOffersByEnd.toList,
      tuplesGamesWithOffersLessTenThousand = tuplesGamesWithOffersLessTenThousand.toList,
      tuplesGamesWithLastOffers = tuplesGamesWithLastOffers.toList,
      tuplesGamesWithBestOffers = tuplesGamesWithBestOffers.toList,
      tuplesGamesWithOffersMoreVisits = tuplesGamesWithOffersMoreVisits.toList
    ))
  }

  def goOffer(offerId: Long, url: String) = Action { implicit request =>
        offerDAO.incrementVisits(offerId)
        Redirect(url, MOVED_PERMANENTLY)

  }
}
