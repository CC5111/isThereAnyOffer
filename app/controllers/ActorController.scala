package controllers

import javax.inject.Inject

import scala.concurrent.duration._
import actors.UpdateActor
import actors.UpdateActor._
import akka.actor.ActorSystem
import models.daos.{GameDAO, OfferDAO, PsStoreDAO}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import akka.pattern.ask
import akka.util.Timeout
import models.entities.PsStore

import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton()
class ActorController @Inject()(gameDAO: GameDAO, offerDAO: OfferDAO, psStoreDAO: PsStoreDAO)
                               (implicit system: ActorSystem, ec: ExecutionContext, ws:WSClient) extends Controller {

  val updateActor = system.actorOf(UpdateActor.props(gameDAO, offerDAO,psStoreDAO), "Updater")
  //val cancellable = system.scheduler.schedule(0.seconds, 12.hours, updateActor, Update)
  implicit val timeout = Timeout(2 minutes)

  def update = Action.async { implicit request =>
    println("Sending message to UpdateActor")
    val resp = updateActor ? Update
    println("Waiting for result")
    val result = Await.result(resp, timeout.duration).asInstanceOf[(String, String)]
    Future(Ok(result._1 + "\n " + result._2))
  }

}
