package models.persistence

import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.driver.JdbcProfile
import models.entities._

/**
  * The companion object.
  */
object SlickTables extends HasDatabaseConfig[JdbcProfile] {

  protected lazy val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import dbConfig.driver.api._

  abstract class BaseTable[T](tag: Tag, name: String) extends Table[T](tag, name) {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  }


  class UserTable(tag: Tag) extends BaseTable[User](tag, "user") {
    def username = column[String]("username")
    def name = column[String]("name")
    def email = column[String]("email")
    def birthdate = column[java.sql.Timestamp]("birthdate")
    def password = column[String]("password")
    def photo = column[String]("photo")

    def * = (id, username, name, email, birthdate, password, photo) <> (User.tupled, User.unapply _)
  }
  val userQ = TableQuery[UserTable]

  class WatchListItemTable(tag: Tag) extends BaseTable[WatchListItem](tag, "watchListItem") {
    def idUser = column[Long]("idUser")
    def idGame = column[Long]("idGame")
    def creationDate = column[java.sql.Timestamp]("creationDate")
    def threshold = column[Int]("threshold")

    def * = (id, idUser, idGame, creationDate, threshold) <> (WatchListItem.tupled, WatchListItem.unapply _)
  }
  val watchListItemQ = TableQuery[WatchListItemTable]

  class OfferTable(tag: Tag) extends BaseTable[Offer](tag, "offer") {
    def link = column[String]("link")
    def idGame = column[Long]("idGame")
    def idStore = column[Long]("idStore")
    def idPlatform = column[Long]("idPlatform")
    def fromDate = column[java.sql.Timestamp]("fromDate")
    def untilDate = column[java.sql.Timestamp]("untilDate")
    def normalPrice = column[Double]("normalPrice")
    def offerPrice = column[Double]("offerPrice")

    def * = (id, link, idGame, idPlatform, idStore, fromDate, untilDate, normalPrice, offerPrice) <> (Offer.tupled, Offer.unapply _)
  }
  val offerQ = TableQuery[OfferTable]

  class GameTable(tag: Tag) extends BaseTable[Game](tag, "game") {
    def name = column[String]("name")
    def cover = column[String]("cover")
    def publisher = column[String]("publisher")
    def developer = column[String]("developer")
    def link = column[String]("link")
    def description = column[String]("description")
    def rating = column[String]("rating")
    def releaseDate = column[java.sql.Timestamp]("releaseDate")
    def gameType = column[String]("gameType")
    def videoLink = column[String]("videoLink")
    def metacritic = column[String]("metacritic")

    def * = (id, name, cover, publisher, developer, link ,description, rating, releaseDate, gameType, videoLink, metacritic) <> (Game.tupled, Game.unapply _)
  }
  val gameQ = TableQuery[GameTable]

  class GameCategoryTable(tag: Tag) extends BaseTable[GameCategory](tag, "gameCategory") {
    def idGame = column[Long]("idGame")
    def idCategory = column[Long]("idCategory")

    def * = (id, idGame, idCategory) <> (GameCategory.tupled, GameCategory.unapply _)
  }
  val gameCategoryQ = TableQuery[GameCategoryTable]

  class CategoryTable(tag: Tag) extends BaseTable[Category](tag, "category") {
    def name = column[String]("name")

    def * = (id, name) <> (Category.tupled, Category.unapply _)
  }
  val categoryQ = TableQuery[CategoryTable]

  class GameGenreTable(tag: Tag) extends BaseTable[GameGenre](tag, "gameGenre") {
    def idGame = column[Long]("idGame")
    def idGenre = column[Long]("idGenre")

    def * = (id, idGame, idGenre) <> (GameGenre.tupled, GameGenre.unapply _)
  }
  val gameGenreQ = TableQuery[GameGenreTable]

  class GenreTable(tag: Tag) extends BaseTable[Genre](tag, "genre") {
    def name = column[String]("name")

    def * = (id, name) <> (Genre.tupled, Genre.unapply _)
  }
  val genreQ = TableQuery[GenreTable]

  class GamePlatformTable(tag: Tag) extends BaseTable[GamePlatform](tag, "gamePlatform") {
    def idGame = column[Long]("idGame")
    def idPlatform = column[Long]("idPlatform")

    def * = (id, idGame, idPlatform) <> (GamePlatform.tupled, GamePlatform.unapply _)
  }
  val gamePlatformQ = TableQuery[GamePlatformTable]

  class PlatformTable(tag: Tag) extends BaseTable[Platform](tag, "platform") {
    def name = column[String]("name")

    def * = (id, name) <> (Platform.tupled, Platform.unapply _)
  }
  val platformQ = TableQuery[PlatformTable]

  class StoreTable(tag: Tag) extends BaseTable[Store](tag, "store") {
    def name = column[String]("name")
    def borderColor = column[String]("borderColor")
    def pointBorderColor = column[String]("pointBorderColor")
    def pointBackgroundColor = column[String]("pointBackgroundColor")
    def pointHoverBackgroundColor = column[String]("pointHoverBackgroundColor")
    def pointHoverBorderColor = column[String]("pointHoverBorderColor")

    def * = (id, name, borderColor, pointBorderColor, pointBackgroundColor, pointHoverBackgroundColor, pointHoverBorderColor) <> (Store.tupled, Store.unapply _)
  }
  val storeQ = TableQuery[StoreTable]

  // Stores
  class G2aStoreTable(tag: Tag) extends BaseTable[G2aStore](tag, "g2aStore") {
    def idGame = column[Long]("idGame")
    def idStore = column[String]("idStore")

    def * = (id, idGame, idStore) <> (G2aStore.tupled, G2aStore.unapply _)
  }
  val g2aStoreQ = TableQuery[G2aStoreTable]

  class GogStoreTable(tag: Tag) extends BaseTable[GogStore](tag, "gogStore") {
    def idGame = column[Long]("idGame")
    def idStore = column[String]("idStore")

    def * = (id, idGame, idStore) <> (GogStore.tupled, GogStore.unapply _)
  }
  val gogStoreQ = TableQuery[GogStoreTable]

  class PsStoreTable(tag: Tag) extends BaseTable[PsStore](tag, "psStore") {
    def idGame = column[Long]("idGame")
    def idStore = column[String]("idStore")

    def * = (id, idGame, idStore) <> (PsStore.tupled, PsStore.unapply _)
  }
  val psStoreQ = TableQuery[PsStoreTable]

  class SteamStoreTable(tag: Tag) extends BaseTable[SteamStore](tag, "steamStore") {
    def idGame = column[Long]("idGame")
    def idStore = column[String]("idStore")

    def * = (id, idGame, idStore) <> (SteamStore.tupled, SteamStore.unapply _)
  }
  val steamStoreQ = TableQuery[SteamStoreTable]

  class XboxStoreTable(tag: Tag) extends BaseTable[XboxStore](tag, "xboxStore") {
    def idGame = column[Long]("idGame")
    def idStore = column[String]("idStore")

    def * = (id, idGame, idStore) <> (XboxStore.tupled, XboxStore.unapply _)
  }
  val xboxStoreQ = TableQuery[XboxStoreTable]
}
