package controllers

import javax.inject.Inject

import actors.SearchActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import models.daos.{GameDAO, GenreDAO, OfferDAO, PlatformDAO}
import models.entities.{Game, Offer}
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, AnyContent, Controller, WebSocket}

import scala.collection.mutable
import scala.concurrent.ExecutionContext

class HomeController @Inject()(gameDAO: GameDAO,
                               offerDAO: OfferDAO,
                               platformDAO: PlatformDAO,
                               genreDAO: GenreDAO)(implicit ec:ExecutionContext, system: ActorSystem, mat:Materializer) extends Controller {

    def index() = Action.async { implicit request =>
        /*
        for {
            platforms <- platformDAO.all
            genres <- genreDAO.all
            offers <- offerDAO.all  //obtencion de las ofertas
            games <- gameDAO.all
        } yield Ok(views.html.home(
            title = "Is There Any Offer - Inicio",
            platforms = platforms.toList,
            genres = genres.toList,
            tuplesOfferGame = for {
                offer <- offers
                game <- games
                tuple <- (offer, game) if offer.idGame == game.id
            } yield tuple
        ))
        */

        platformDAO.all.flatMap { platforms =>
            genreDAO.all.flatMap { genres =>
                offerDAO.all.flatMap { offers =>
                    gameDAO.all.map { games =>
                        var tuplaRasca = mutable.MutableList[(Offer, Game)]()
                        var i = 0
                        var j = 0
                        while (i < offers.length) {
                            val offer = offers(i)
                            val game = games.filter((game) => game.id == offer.idGame).head
                            tuplaRasca += ((offer, game))
                            i+=1
                        }

                        Ok(views.html.home(
                            title = "Is There Any Offer - Inicio",
                            platforms = platforms.toList,
                            genres = genres.toList,
                            tuplesOfferGame = tuplaRasca.toList
                        ))
                    }
                }
            }
        }
    }

    def search() = Action(parse.urlFormEncoded) { request =>
        Redirect(routes.ResultController.index(request.body.get("search").head.head))
    }

    // JsValue ~ JSON
    def socket = WebSocket.accept[JsValue, JsValue] { request =>
        ActorFlow.actorRef(out => SearchActor.props(out,gameDAO))
    }
}
