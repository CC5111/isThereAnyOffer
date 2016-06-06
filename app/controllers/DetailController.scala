package controllers

import javax.inject.Inject

import actors.SearchActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import models.daos.GameDAO
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, Controller, WebSocket}

import scala.concurrent.ExecutionContext

class DetailController @Inject()(game: GameDAO)(implicit ec:ExecutionContext, system: ActorSystem, mat:Materializer) extends Controller {

    def index(id: Long) = Action { implicit request =>
        Ok(views.html.detail(title = "Is There Any Offer - Detalle"))

    }




}
