package controllers

import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, Controller, WebSocket}

class HomeController extends Controller {

    def index() = Action { implicit request =>
        Ok(views.html.index)
    }

    def searchResults() = Action { implicit request =>
        Ok(views.html.searchResults)
    }

    // JsValue ~ JSON
    def socket = WebSocket.accept[JsValue, JsValue] { request =>
        ActorFlow.actorRef(out => SearchActor.props(out))
    }


}
