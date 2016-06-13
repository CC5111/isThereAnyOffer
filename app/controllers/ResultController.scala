package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import models.daos.GameDAO
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

class ResultController @Inject()(gameDAO: GameDAO)(implicit ec:ExecutionContext, system: ActorSystem, mat:Materializer) extends Controller {

  def index(query: String) = Action.async { implicit request =>
    for {
      games <- gameDAO.searchByName(query)
    } yield Ok(views.html.result(title = "Is There Any Offer - Result", query = query, games = games.toList))
  }
}
