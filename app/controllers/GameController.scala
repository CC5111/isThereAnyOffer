package controllers

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.util.Timeout
import models.daos._
import models.entities._
import play.api.mvc.{Action, Controller}
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class GameController @Inject()(gameDAO: GameDAO, gamePlatformDAO: GamePlatformDAO, gameCategoryDAO: GameCategoryDAO,
                               psStoreDAO: PsStoreDAO, xboxStoreDAO: XboxStoreDAO, steamDAO: SteamDAO, gogDAO: GogDAO)
                              (implicit ec:ExecutionContext, system: ActorSystem, mat:Materializer) extends Controller {

  val gameForm: Form[GameData] = Form(
    mapping(
      "name" -> nonEmptyText,
      "publisher" -> nonEmptyText,
      "developer" -> nonEmptyText,
      "link" -> nonEmptyText,
      "description" -> nonEmptyText,
      "rating" -> nonEmptyText,
      "releaseDate" -> jodaDate,
      "gameType" -> nonEmptyText,
      "video" -> nonEmptyText,
      "score" -> nonEmptyText,
      "platform" -> mapping(
        "ps3" -> boolean,
        "ps4" -> boolean,
        "x360" -> boolean,
        "xone" -> boolean,
        "pc" -> boolean
      )(PlatformData.apply)(PlatformData.unapply),
      "category" -> mapping(
        "single" -> boolean,
        "coop" -> boolean,
        "multi" -> boolean
      )(CategoryData.apply)(CategoryData.unapply),
      "store_ids" -> mapping(
        "psStore" -> text,
        "xboxStore" -> text,
        "steam" -> text,
        "gog" -> text
      )(StoreData.apply)(StoreData.unapply)
    )(GameData.apply)(GameData.unapply) verifying("Failed form constraints!", fields => fields match {
      case gameData => validate(gameData).isDefined
    })
  )

  def validate(data: GameData) = {
    if ((data.platform.ps3 || data.platform.ps4) && data.store_ids.psStore == "")
      None
    if ((data.platform.x360 || data.platform.xone) && data.store_ids.xboxStore == "")
      None
    if (data.platform.pc && (data.store_ids.steam == "" && data.store_ids.gog == ""))
      None
    if (!data.category.single && !data.category.coop && !data.category.multi)
      None
    Some(GameData)
  }

  def index = Action {
    Ok(views.html.game(gameForm))
  }

  def processForm = Action.async { implicit request =>
    gameForm.bindFromRequest.fold(
      formWithErrors => {
        Future(BadRequest("error"))
      },
      gameData => {
        val newGame = Game(0, gameData.name, "images/games/placeholder.png", gameData.publisher,
                          gameData.developer, gameData.link, gameData.description, gameData.rating,
                          new Timestamp(gameData.releaseDate.getMillis), gameData.gameType, gameData.video,
                          gameData.score)
        val id = Await.result(gameDAO.insert(newGame), Timeout(1 minute).duration)
        if (gameData.platform.ps4) gamePlatformDAO.insert(new GamePlatform(0, id, 1))
        if (gameData.platform.ps3) gamePlatformDAO.insert(new GamePlatform(0, id, 2))
        if (gameData.platform.x360) gamePlatformDAO.insert(new GamePlatform(0, id, 4))
        if (gameData.platform.xone) gamePlatformDAO.insert(new GamePlatform(0, id, 3))
        if (gameData.platform.pc) gamePlatformDAO.insert(new GamePlatform(0, id, 5))
        if (gameData.category.single) gameCategoryDAO.insert(new GameCategory(0, id, 3))
        if (gameData.category.coop) gameCategoryDAO.insert(new GameCategory(0, id, 2))
        if (gameData.category.multi) gameCategoryDAO.insert(new GameCategory(0, id, 1))
        if (gameData.store_ids.psStore != "") psStoreDAO.insert(new PsStore(0, id, gameData.store_ids.psStore))
        if (gameData.store_ids.xboxStore != "") xboxStoreDAO.insert(new XboxStore(0, id, gameData.store_ids.xboxStore))
        if (gameData.store_ids.gog != "") gogDAO.insert(new GogStore(0, id, gameData.store_ids.gog))
        if (gameData.store_ids.steam != "") steamDAO.insert(new SteamStore(0, id, gameData.store_ids.steam))
        Future(Redirect(routes.DetailController.index(id)))
      }
    )
  }

}