package controllers

import javax.inject.{Inject, Singleton}

import scala.concurrent.duration._
import actors.UpdateActor
import actors.UpdateActor._
import akka.actor.ActorSystem
import models.daos._
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class ActorController @Inject()(offerDAO: OfferDAO, psStoreDAO: PsStoreDAO, gogDAO: GogDAO, steamDAO: SteamDAO)
                               (implicit system: ActorSystem, ec: ExecutionContext, ws:WSClient) extends Controller {

  val updateActor = system.actorOf(UpdateActor.props(offerDAO, psStoreDAO, gogDAO, steamDAO), "Updater")
  //val cancellable = system.scheduler.schedule(0.seconds, 12.hours, updateActor, Update)
  implicit val timeout = Timeout(5 minutes)

  def update = Action.async { implicit request =>
    println("Sending message to UpdateActor")
    val resp = updateActor ? Update
    println("Waiting for result")
    val result = Await.result(resp, timeout.duration).asInstanceOf[(String, String, String)]
    Future(Ok(result._1 + "\n" + result._2 + "\n" + result._3))
  }

  def updateGOG = Action.async{ implicit request =>
    println("Sending message to UpdateActor")
    val resp = updateActor ? UpdateGOG
    println("Waiting for result")
    val result = Await.result(resp, timeout.duration).asInstanceOf[String]
    Future(Ok(result))
  }

  def updatePs = Action.async{ implicit request =>
    println("Sending message to UpdateActor")
    val resp = updateActor ? UpdatePs
    println("Waiting for result")
    val result = Await.result(resp, timeout.duration).asInstanceOf[String]
    Future(Ok(result))
  }

  def updateSteam = Action.async{ implicit request =>
    println("Sending message to UpdateActor")
    val resp = updateActor ? UpdateSteam
    println("Waiting for result")
    val result = Await.result(resp, timeout.duration).asInstanceOf[String]
    Future(Ok(result))
  }

}
