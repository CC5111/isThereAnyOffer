package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import models.daos.{GameDAO, OfferDAO}
import org.omg.CosNaming.NamingContextPackage.NotFound
import play.api.mvc.{Action, Controller, WebSocket}

import scala.concurrent.{ExecutionContext, Future}

class DetailController @Inject()(gameDAO: GameDAO, offerDAO: OfferDAO)(implicit ec:ExecutionContext, system: ActorSystem, mat:Materializer) extends Controller {

  def index(id: Long) = Action.async { implicit request =>
    for {
      game <- gameDAO.findById(id)
      tuples <- gameDAO.offersByGame(id)
    } yield Ok(views.html.detail("Is There Any Offer - Detalle", game match {
      case Some(g) => g
      case None => throw new NotFound
    }, tuples.toList))
  }
}
