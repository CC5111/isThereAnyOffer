package controllers

import javax.inject.Inject

import actors.SearchActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import models.daos.{GameDAO, OfferDAO, PlatformDAO}
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, AnyContent, Controller, WebSocket}

import scala.concurrent.ExecutionContext

class HomeController @Inject()(game: GameDAO, offer: OfferDAO, platform: PlatformDAO)(implicit ec:ExecutionContext, system: ActorSystem, mat:Materializer) extends Controller {

    def index() = Action.async { implicit request =>
        platform.all.map { platforms =>
            Ok(views.html.home(title = "Is There Any Offer - Inicio", platforms = platforms.toList))
        }
    }

    def search() = Action(parse.urlFormEncoded) { request =>
        Redirect(routes.ResultController.index(request.body.get("search").head.head))
    }

    // JsValue ~ JSON
    def socket = WebSocket.accept[JsValue, JsValue] { request =>
        ActorFlow.actorRef(out => SearchActor.props(out,game))
    }
}
