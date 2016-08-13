package actors

import javax.inject.Inject

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import models.daos.{GameDAO, GogDAO, OfferDAO, PsStoreDAO}
import play.api.libs.ws.WSClient

import scala.concurrent.{Await, ExecutionContext}


object UpdateActor {
  case object Update
  case object UpdateGOG
  case object UpdatePs
  def props(gameDAO: GameDAO, offerDAO: OfferDAO, psStoreDAO: PsStoreDAO, gogDAO: GogDAO)(implicit system: ActorSystem, ec: ExecutionContext, ws:WSClient) =
    Props(new UpdateActor(gameDAO, offerDAO, psStoreDAO, gogDAO)(system, ec, ws))
}
class UpdateActor @Inject() (gameDAO: GameDAO, offerDAO: OfferDAO, psStoreDAO: PsStoreDAO, gogDAO: GogDAO)
                            (implicit system: ActorSystem, ec: ExecutionContext, ws:WSClient) extends Actor{
  import UpdateActor._
  import context._

  val gogActor = context.actorOf(GogActor.props(gameDAO, offerDAO, gogDAO), "GOG-Actor")
  val psActor = context.actorOf(PsActor.props(gameDAO, offerDAO, psStoreDAO), "PS-Actor")
  implicit val timeout = Timeout(1 minute)

  def receive = {
    case Update => {
      println("Update message received. Sending messages to GOGActor and PsActor")
      val gogResult = gogActor ? actors.GogActor.Update
      val psResult = psActor ? actors.PsActor.Update
      /*psResult.onSuccess{
        case r: String=>
          println("Success!!, recibi", r)
        case _ => println("recibi otra cosa")
      }*/


      println("UpdateActor: Waiting response from GOGActor")
      val gogMessage = Await.result(gogResult, timeout.duration).asInstanceOf[String]
      println("UpdateActor: Waiting response from PsActor")
      val psMessage = Await.result(psResult, timeout.duration).asInstanceOf[String]

      println("Response from GOGActor and PsActor received. Sending response to ActorController")
      sender() ! (gogMessage, psMessage)
    }

    case UpdateGOG => {
      println("UpdateGOG message received. Sending message to GOGActor")
      val gogResult = gogActor ? actors.GogActor.Update
      println("UpdateActor: Waiting response from GogActor")
      val gogMessage = Await.result(gogResult, timeout.duration*2).asInstanceOf[String]
      sender ! gogMessage
    }

    case UpdatePs => {
      println("UpdatePs message received. Sending message to PsActor")
      val psResult = psActor ? actors.PsActor.Update
      println("UpdateActor: Waiting response from PsActor")
      val psMessage = Await.result(psResult, timeout.duration).asInstanceOf[String]
      sender ! psMessage
    }

  }
}
