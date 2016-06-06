package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import models.daos.GameDAO
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

class ResultController @Inject()(game: GameDAO)(implicit ec:ExecutionContext, system: ActorSystem, mat:Materializer) extends Controller {

    def index(query: String) = Action { implicit request =>
        Ok(views.html.result(title = "Is There Any Offer - Result"))

    }




}
