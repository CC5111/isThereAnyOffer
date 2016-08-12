package controllers

import javax.inject.{Inject, Singleton}
import akka.actor.ActorSystem
import akka.stream.Materializer
import models.daos.{CategoryDAO, GameDAO, GenreDAO, OfferDAO}
import org.omg.CosNaming.NamingContextPackage.NotFound
import play.api.mvc.{Action, Controller, WebSocket}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DetailController @Inject()(gameDAO: GameDAO, offerDAO: OfferDAO, genreDAO: GenreDAO, categoryDAO: CategoryDAO)(implicit ec:ExecutionContext, system: ActorSystem, mat:Materializer) extends Controller {

    def index(id: Long) = Action.async { implicit request =>
        gameDAO.findById(id).flatMap { g =>
            if (g.isEmpty) {
                Future(NotFound("kfsjfkjs"))
            } else {
                for {
                    tuples <- gameDAO.offersByGame(id)
                    genres <- genreDAO.genresGame(g.get)
                    categories <- categoryDAO.categoriesGame(g.get)
                } yield Ok(views.html.detail("Detalle", g.get, tuples.toList, genres.toList, categories.toList))
            }
        }
    }
}
