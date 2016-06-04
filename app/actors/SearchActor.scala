package actors

import java.sql.Timestamp

import akka.actor.{Actor, ActorRef, Props}
import models.daos.{GameDAO}
import play.api.libs.json.{JsValue, Json}

object SearchActor{
    def props(out: ActorRef, gameDAO: GameDAO) =
        Props(classOf[SearchActor], out, gameDAO)
}

class SearchActor(out: ActorRef, gameDAO: GameDAO) extends Actor {

    import context._

    println(self.path)   // path del actor

    def receive = {
        case msg: String => {

            val results = gameDAO.searchByName(msg)
            out ! Json.toJson(results)
        }
    }
}

