package actors


import akka.actor.{Actor, ActorRef, Props}
import models.daos.GameDAO

object SteamActor {
    case class Query(game: String)
    case class Update()
    def props(out: ActorRef, gameDAO: GameDAO) =
        Props(classOf[SteamActor], out, gameDAO)
}

class SteamActor extends Actor{
    import context._
    import actors.SteamActor._

    def receive = {
        case Query(game) => {
            // Obtener precios para $game
        }
        case Update() => {
            // Actualizar precios de la DB
        }
    }

}
