package controllers

import javax.inject.Inject
import akka.actor.ActorSystem
import akka.stream.Materializer
import models.daos.GameDAO

import play.api.mvc.{Action, Controller, WebSocket}

import scala.concurrent.{ExecutionContext, Future}

class DetailController @Inject()(game: GameDAO)(implicit ec:ExecutionContext, system: ActorSystem, mat:Materializer) extends Controller {

    def index(id: Long) = Action.async { implicit request =>
        game.findById(id).flatMap{
            case Some(gameData) => Future(Ok(views.html.detail("Is There Any Offer - Detalle", gameData)))
            case _ => Future(NotFound)
        }
    }
}
