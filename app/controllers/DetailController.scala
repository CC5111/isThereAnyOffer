package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import models.daos.{GameDAO, GenreDAO, OfferDAO}
import org.omg.CosNaming.NamingContextPackage.NotFound
import play.api.mvc.{Action, Controller, WebSocket}

import scala.concurrent.{ExecutionContext, Future}

class DetailController @Inject()(gameDAO: GameDAO, offerDAO: OfferDAO, genreDAO: GenreDAO)(implicit ec:ExecutionContext, system: ActorSystem, mat:Materializer) extends Controller {

    def index(id: Long) = Action.async { implicit request =>
        gameDAO.findById(id).flatMap { g =>
            if (g.isEmpty) {
                Future(NotFound("kfsjfkjs"))
            } else {
                for {
                    tuples <- gameDAO.offersByGame(id)
                    genres <- genreDAO.genresGame(g.get)
                } yield Ok(views.html.detail("Detalle", g.get, tuples.toList, genres.toList))
            }
        }
    }
}
