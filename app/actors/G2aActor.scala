package actors

import akka.actor.{Actor, ActorRef, Props}
import models.daos.GameDAO

object G2aActor {
    case class Query(game: String)
    case class Update()
    def props(out: ActorRef, gameDAO: GameDAO) =
        Props(classOf[SteamActor], out, gameDAO)
}

class G2aActor extends Actor{
    import context._
    import actors.G2aActor._

    // https://www.g2a.com/lucene/search/quick?&phrase=<query>

    def receive = {
        case Query(game) => {
            // Obtener precios para $game
        }
        case Update() => {
            // Actualizar precios de la DB
        }
    }

}