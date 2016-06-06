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

    def * = (id, username, name, email, birthdate, password, photo)  <> (User.tupled, User.unapply _)
  }
  val userQ = TableQuery[UserTable]

  class WatchListItemTable(tag: Tag) extends BaseTable[WatchListItem](tag, "watchListItem") {
    def idUser = column[Long]("idUser")
    def idGame = column[Long]("idGame")
    def creationDate = column[java.sql.Timestamp]("creationDate")
    def threshold = column[Int]("threshold")

    def * = (id, idUser, idGame, creationDate, threshold)  <> (WatchListItem.tupled, WatchListItem.unapply _)
  }
  val watchListItemQ = TableQuery[WatchListItemTable]

  class OfferTable(tag: Tag) extends BaseTable[Offer](tag, "offer") {
    def link = column[String]("link")
    def idGame = column[Long]("idGame")
    def fromDate = column[java.sql.Timestamp]("fromDate")
    def untilDate = column[java.sql.Timestamp]("untilDate")
    def normalPrice = column[Double]("normalPrice")
    def offerPrice = column[Double]("offerPrice")

    def * = (id, link, idGame, fromDate, untilDate, normalPrice, offerPrice)  <> (Offer.tupled, Offer.unapply _)
  }
  val offerQ = TableQuery[OfferTable]

  class GameTable(tag: Tag) extends BaseTable[Game](tag, "game") {
    def name = column[String]("name")
    def cover = column[String]("cover")
    def publisher = column[String]("publisher")
    def rating = column[String]("rating")
    def releaseDate = column[java.sql.Timestamp]("releaseDate")
    def typeGame = column[String]("typeGame")

    def * = (id, name, cover, publisher, rating, releaseDate, typeGame)  <> (Game.tupled, Game.unapply _)
  }
  val gameQ = TableQuery[GameTable]

  class GameCategoryTable(tag: Tag) extends BaseTable[GameCategory](tag, "gameCategory") {
    def idGame = column[Long]("idGame")
    def idCategory = column[Long]("idCategory")

    def * = (id, idGame, idCategory)  <> (GameCategory.tupled, GameCategory.unapply _)
  }
  val gameCategoryQ = TableQuery[GameCategoryTable]

  class CategoryTable(tag: Tag) extends BaseTable[Category](tag, "category") {
    def name = column[String]("name")

    def * = (id, name)  <> (Category.tupled, Category.unapply _)
  }
  val categoryQ = TableQuery[CategoryTable]

  class GameGenreTable(tag: Tag) extends BaseTable[GameGenre](tag, "gameGenre") {
    def idGame = column[Long]("idGame")
    def idGenre = column[Long]("idGenre")

    def * = (id, idGame, idGenre)  <> (GameGenre.tupled, GameGenre.unapply _)
  }
  val gameGenreQ = TableQuery[GameGenreTable]

  class GenreTable(tag: Tag) extends BaseTable[Genre](tag, "genre") {
    def name = column[String]("name")

    def * = (id, name)  <> (Genre.tupled, Genre.unapply _)
  }
  val genreQ = TableQuery[GenreTable]

  class GamePlatformTable(tag: Tag) extends BaseTable[GamePlatform](tag, "gamePlatform") {
    def idGame = column[Long]("idGame")
    def idPlatform = column[Long]("idPlatform")

    def * = (id, idGame, idPlatform)  <> (GamePlatform.tupled, GamePlatform.unapply _)
  }
  val gamePlatformQ = TableQuery[GamePlatformTable]

  class PlatformTable(tag: Tag) extends BaseTable[Platform](tag, "platform") {
    def name = column[String]("name")

    def * = (id, name)  <> (Platform.tupled, Platform.unapply _)
  }
  val platformQ = TableQuery[PlatformTable]



  case class SimpleSupplier(name: String, desc: String)

  class SuppliersTable(tag: Tag) extends BaseTable[Supplier](tag, "suppliers") {
    def name = column[String]("name")
    def desc = column[String]("desc")
    def * = (id, name, desc) <> (Supplier.tupled, Supplier.unapply)
  }

  val suppliersTableQ : TableQuery[SuppliersTable] = TableQuery[SuppliersTable]

}
