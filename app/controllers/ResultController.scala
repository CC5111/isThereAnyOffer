package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.Materializer
import models.daos._
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

@Singleton
class ResultController @Inject()(gameDAO: GameDAO,
                                 offerDAO: OfferDAO,
                                 platformDAO: PlatformDAO,
                                 genreDAO: GenreDAO,
                                 categoryDAO: CategoryDAO)(implicit ec:ExecutionContext, system: ActorSystem, mat:Materializer) extends Controller {

    def index(query: String) = Action.async { implicit request =>

        for {
            tuplesPlatformCount <- platformDAO.allPlatformsOffersWithCount
            tuplesGenreCount <- genreDAO.allGenresWithCount
            tuplesCategoryCount <- categoryDAO.allCategoriesWithCount
        } yield Ok(views.html.result(
            title = "Búsqueda",
            query = query,
            tuplesPlatformCount = tuplesPlatformCount.toList,
            tuplesGenreCount = tuplesGenreCount.toList,
            tuplesCategoryCount = tuplesCategoryCount.toList
        ))
    }
}
