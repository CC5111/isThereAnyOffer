package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import models.daos.{GameDAO, OfferDAO}
import play.api.mvc.{Action, Controller, WebSocket}

import scala.concurrent.{ExecutionContext, Future}

class DetailController @Inject()(game: GameDAO, offer: OfferDAO)(implicit ec:ExecutionContext, system: ActorSystem, mat:Materializer) extends Controller {

    def index(id: Long) = Action.async { implicit request =>

       game.findById(id).flatMap{
            case Some(game) =>
                for {
                    offers_data <- offer.offersByGame(id)
                } yield Ok(views.html.detail("Is There Any Offer - Detalle", game, offers_data.toList))

            case _ => Future(NotFound)
        }

    }
}
