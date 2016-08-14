package actors

import javax.inject.Inject

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import models.daos.{GameDAO, GogDAO, OfferDAO, PsStoreDAO, SteamDAO}
import play.api.libs.ws.WSClient

import scala.concurrent.{Await, ExecutionContext}


object UpdateActor {
  case object Update
  case object UpdateGOG
  case object UpdatePs
  case object UpdateSteam
  def props(gameDAO: GameDAO, offerDAO: OfferDAO, psStoreDAO: PsStoreDAO, gogDAO: GogDAO, steamDAO: SteamDAO)
           (implicit system: ActorSystem, ec: ExecutionContext, ws:WSClient) =
    Props(new UpdateActor(gameDAO, offerDAO, psStoreDAO, gogDAO, steamDAO)(system, ec, ws))
}
class UpdateActor @Inject() (gameDAO: GameDAO, offerDAO: OfferDAO, psStoreDAO: PsStoreDAO, gogDAO: GogDAO, steamDAO: SteamDAO)
                            (implicit system: ActorSystem, ec: ExecutionContext, ws:WSClient) extends Actor{
  import UpdateActor._
  import context._

  val gogActor = context.actorOf(GogActor.props(gameDAO, offerDAO, gogDAO), "GOG-Actor")
  val psActor = context.actorOf(PsActor.props(gameDAO, offerDAO, psStoreDAO), "PS-Actor")
  val steamActor = context.actorOf(SteamActor.props(offerDAO, steamDAO), "Steam-Actor")
  implicit val timeout = Timeout(1 minute)

  def receive = {
    case Update => {
      println("Update message received. Sending messages to GOGActor and PsActor")
      val gogResult = gogActor ? actors.GogActor.Update
      val psResult = psActor ? actors.PsActor.Update
      val steamResult = steamActor ? actors.SteamActor.Update

      /*psResult.onSuccess{
        case r: String=>
          println("Success!!, recibi", r)
        case _ => println("recibi otra cosa")
      }*/

      println("UpdateActor: Waiting response from GOGActor")
      val gogMessage = Await.result(gogResult, timeout.duration).asInstanceOf[String]
      println("UpdateActor: Received response from GOGActor")
      println("UpdateActor: Waiting response from PsActor")
      val psMessage = Await.result(psResult, timeout.duration).asInstanceOf[String]
      println("UpdateActor: Received response from PSActor")
      println("UpdateActor: Waiting response from SteamActor")
      val steamMessage = Await.result(steamResult, timeout.duration).asInstanceOf[String]
      println("UpdateActor: Received response from SteamActor")

      println("UpdateActor: All messages received. Sending response to ActorController")
      sender() ! (gogMessage, psMessage, steamMessage)
    }

    case UpdateGOG => {
      println("UpdateGOG message received. Sending message to GOGActor")
      val gogResult = gogActor ? actors.GogActor.Update
      println("UpdateActor: Waiting response from GogActor")
      val gogMessage = Await.result(gogResult, timeout.duration*2).asInstanceOf[String]
      println("UpdateActor: Message received. Sending response to ActorController")
      sender ! gogMessage
    }

    case UpdatePs => {
      println("UpdatePs message received. Sending message to PsActor")
      val psResult = psActor ? actors.PsActor.Update
      println("UpdateActor: Waiting response from PsActor")
      val psMessage = Await.result(psResult, timeout.duration).asInstanceOf[String]
      println("UpdateActor: Message received. Sending response to ActorController")
      sender ! psMessage
    }
    case UpdateSteam => {
      println("UpdateSteam message received. Sending message to SteamActor")
      val steamResult = steamActor ? actors.SteamActor.Update
      println("UpdateActor: Waiting response from SteamActor")
      val steamMessage = Await.result(steamResult, timeout.duration).asInstanceOf[String]
      println("UpdateActor: Message received. Sending response to ActorController")
      sender ! steamMessage
    }

  }
}
