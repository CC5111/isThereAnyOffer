package actors

import akka.actor.{Actor, ActorRef, Props}
import models.daos.GameDAO

object XboxActor {
    case class Query(game: String)
    case class Update()
    def props(out: ActorRef, gameDAO: GameDAO) =
        Props(classOf[SteamActor], out, gameDAO)
}

class XboxActor extends Actor{
    import context._
    import actors.XboxActor._

    // http://marketplace.xbox.com/en-US/SiteSearch/xbox/?query=<query>

    def receive = {
        case Query(game) => {
            // Obtener precios para $game
        }
        case Update() => {
            // Actualizar precios de la DB
        }
    }

}