package actors


import akka.actor.{Actor, ActorRef, Props}
import models.daos.GameDAO

object GogActor {
    case class Query(game: String)
    case class Update()
    def props(out: ActorRef, gameDAO: GameDAO) =
        Props(classOf[SteamActor], out, gameDAO)
}

class GogActor extends Actor{
    import context._
    import actors.GogActor._

    // https://www.gog.com/games/ajax/filtered?mediaType=game&search=<query>

    def receive = {
        case Query(game) => {
            // Obtener precios para $game
        }
        case Update() => {
            // Actualizar precios de la DB
        }
    }

}