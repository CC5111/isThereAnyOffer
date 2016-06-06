package actors

import akka.actor.{Actor, ActorRef, Props}
import models.daos.GameDAO

object PsActor {
    case class Query(game: String)
    case class Update()
    def props(out: ActorRef, gameDAO: GameDAO) =
        Props(classOf[SteamActor], out, gameDAO)
}

class PsActor extends Actor{
    import context._
    import actors.PsActor._

    // https://store.playstation.com/store/api/chihiro/00_09_000/tumbler/CL/es/999/<query>?suggested_size=5&mode=game

    def receive = {
        case Query(game) => {
            // Obtener precios para $game
        }
        case Update() => {
            // Actualizar precios de la DB
        }
    }

}