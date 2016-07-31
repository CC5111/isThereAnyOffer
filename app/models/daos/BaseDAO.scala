package models.daos

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}

import models.persistence.SlickTables
import models.persistence.SlickTables._
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.lifted.CanBeQueryCondition

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import models.entities._

@Singleton
class GameDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends BaseDAO[GameTable, Game]{
  import dbConfig.driver.api._

  protected val tableQ = SlickTables.gameQ

  def all: Future[Seq[Game]] = {
    db.run(tableQ.result)
  }


  def offersByGame(id :Long): Future[Seq[(Offer, Platform)]] = {
    val offerQ = SlickTables.offerQ
    val platformQ = SlickTables.platformQ

    val query = for {
      (offer, platform) <- offerQ join platformQ on (_.idPlatform === _.id) if offer.idGame === id
    } yield (offer, platform)

    db.run(query.result)
  }

  def searchByName(name: String): Future[Seq[Game]] = {
    val query = for {
      game <- tableQ if game.name.toLowerCase.like("%" + name.toLowerCase() + "%")
    } yield game
    db.run(query.result)
  }
}

@Singleton
class OfferDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends BaseDAO[OfferTable, Offer]{
  import dbConfig.driver.api._

  protected val tableQ = SlickTables.offerQ

  def all: Future[Seq[Offer]] = {
    db.run(tableQ.result)
  }

  def actualOffers: Future[Seq[(Offer, Game, Platform)]] = {
    val gameQ = SlickTables.gameQ
    val platformQ = SlickTables.platformQ

    val dateNow = new Timestamp(new java.util.Date().getTime)

    val query = for {
      ((offer, game) , platform) <- tableQ join gameQ on (_.idGame === _.id) join platformQ on (_._1.idPlatform === _.id)
      if offer.untilDate.>(dateNow) && offer.normalPrice =!= offer.offerPrice
    } yield (offer, game, platform)

    db.run(query.sortBy(_._2.name).result)
  }

  def filterOffers(pageNumber: Int = 1, pageSize:Int = 3, platformFilter: String = ""): Future[Seq[(Offer, Game, Platform)]] = {
    val gameQ = SlickTables.gameQ
    val platformQ = SlickTables.platformQ

    val dateNow = new Timestamp(new java.util.Date().getTime)

    val query = (for {
      ((offer, game) , platform) <- tableQ join gameQ on (_.idGame === _.id) join platformQ on (_._1.idPlatform === _.id)
      if offer.untilDate.>(dateNow) && offer.normalPrice =!= offer.offerPrice
    } yield (offer, game, platform))
      .sortBy(_._2.name)

    var queryFilter = query
    if (platformFilter != "") queryFilter = query.filter((tuple) => tuple._3.name === platformFilter)

    val offset = (pageNumber - 1) * pageSize

    db.run(queryFilter.drop(offset).take(pageSize).result)
  }

  def lastGamesWithOffers: Future[Seq[(Offer, Game, Platform)]] = {
    val gameQ = SlickTables.gameQ
    val platformQ = SlickTables.platformQ

    val dateNow = new Timestamp(new java.util.Date().getTime)

    val query = for {
      ((offer, game) , platform) <- tableQ join gameQ on (_.idGame === _.id) join platformQ on (_._1.idPlatform === _.id)
      if offer.untilDate.>(dateNow) && offer.normalPrice =!= offer.offerPrice
    } yield (offer, game, platform)
    db.run(query.sortBy(_._2.releaseDate.desc).take(4).result)
  }
}

@Singleton
class PlatformDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends BaseDAO[PlatformTable, Platform]{
  import dbConfig.driver.api._

  protected val tableQ = SlickTables.platformQ

  def all: Future[Seq[Platform]] = {
    db.run(tableQ.result)
  }

  def allPlatformsOffersWithCount: Future[Seq[(Platform, Int)]] = {
    val offerQ = SlickTables.offerQ

    val platformsWithOffers = (for {
      (platform, offer) <- tableQ join offerQ on (_.id === _.idPlatform)
    } yield (platform, offer))
      .groupBy(_._1).map {
      case (plat, offers) => (plat, offers.length)
    }

    val platformsWithoutOffers = for {
    platform <- tableQ if !platformsWithOffers.filter(_._1.id === platform.id).exists
    } yield (platform, 0)

    val query = (platformsWithOffers union platformsWithoutOffers) sortBy(_._1.name.asc)
    db.run(query.result)
  }
}

@Singleton
class GenreDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends BaseDAO[GenreTable, Genre]{
  import dbConfig.driver.api._

  protected val tableQ = SlickTables.genreQ

  def all: Future[Seq[Genre]] = {
    db.run(tableQ.result)
  }

  def genresGame(game: Game): Future[Seq[Genre]] = {
    val gameGenreQ = SlickTables.gameGenreQ

    val query = for {
      (gameGenre, genre) <- gameGenreQ join tableQ on (_.idGenre === _.id)
      if gameGenre.idGame === game.id
    } yield genre

    db.run(query.result)
  }

  def allGamesWithGenres(): Future[Seq[(Game, Genre)]] = {
    val gameQ = SlickTables.gameQ
    val gameGenreQ = SlickTables.gameGenreQ

    val query = for {
      ((game, gameGenre), genre) <- gameQ join gameGenreQ on (_.id === _.idGame) join tableQ on (_._2.idGenre === _.id)
    } yield (game, genre)
    db.run(query.result)
  }

  def allGenresWithCount: Future[Seq[(Genre, Int)]] = {
    val gameGenreQ = SlickTables.gameGenreQ
    val offerQ = SlickTables.offerQ

    val genresWithGames = (for {
      ((genre, gameGenre), offer) <- tableQ join gameGenreQ on (_.id === _.idGenre) join offerQ on (_._2.idGame === _.idGame)
    } yield (genre, gameGenre))
      .groupBy(_._1).map {
      case (gen, gameGen) => (gen, gameGen.length)
    }

    val genresWithoutGames = for {
      genre <- tableQ if !genresWithGames.filter(_._1.id === genre.id).exists
    } yield (genre, 0)

    val query = (genresWithGames union genresWithoutGames).sortBy(_._1.name.asc)
    db.run(query.result)
  }
}

@Singleton
class CategoryDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends BaseDAO[CategoryTable, Category]{
  import dbConfig.driver.api._

  protected val tableQ = SlickTables.categoryQ

  def all: Future[Seq[Category]] = {
    db.run(tableQ.result)
  }

  def categoriesGame(game: Game): Future[Seq[Category]] = {
    val gameCategoryQ = SlickTables.gameCategoryQ

    val query = for {
      (gameCategory, category) <- gameCategoryQ join tableQ on (_.idCategory === _.id)
      if gameCategory.idGame === game.id
    } yield category

    db.run(query.result)
  }

  def allGamesWithCategories(): Future[Seq[(Game, Category)]] = {
    val gameQ = SlickTables.gameQ
    val gameCategoryQ = SlickTables.gameCategoryQ

    val query = for {
      ((game, gameCategory), category) <- gameQ join gameCategoryQ on (_.id === _.idGame) join tableQ on (_._2.idCategory === _.id)
    } yield (game, category)
    db.run(query.result)
  }

  def allCategoriesWithCount: Future[Seq[(Category, Int)]] = {
    val gameCategoryQ = SlickTables.gameCategoryQ
    val offerQ = SlickTables.offerQ

    val categoriesWithGames = (for {
      ((category, gameCategory), offer) <- tableQ join gameCategoryQ on (_.id === _.idCategory) join offerQ on (_._2.idGame === _.idGame)
    } yield (category, gameCategory))
      .groupBy(_._1).map {
      case (cat, gameCat) => (cat, gameCat.length)
    }

    val categoriesWithoutGames = for {
      category <- tableQ if !categoriesWithGames.filter(_._1.id === category.id).exists
    } yield (category, 0)

    val query = (categoriesWithGames union categoriesWithoutGames).sortBy(_._1.name.asc)
    db.run(query.result)
  }
}

trait AbstractBaseDAO[T,A] {
  def insert(row : A): Future[Long]
  def insert(rows : Seq[A]): Future[Seq[Long]]
  def update(row : A): Future[Int]
  def update(rows : Seq[A]): Future[Unit]
  def findById(id : Long): Future[Option[A]]
  def findByFilter[C : CanBeQueryCondition](f: (T) => C): Future[Seq[A]]
  def deleteById(id : Long): Future[Int]
  def deleteById(ids : Seq[Long]): Future[Int]
  def deleteByFilter[C : CanBeQueryCondition](f:  (T) => C): Future[Int]
}


abstract class BaseDAO[T <: BaseTable[A], A <: BaseEntity]() extends AbstractBaseDAO[T,A] with HasDatabaseConfig[JdbcProfile] {
  protected lazy val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import dbConfig.driver.api._

  protected val tableQ: TableQuery[T]

  def insert(row : A): Future[Long] ={
    insert(Seq(row)).map(_.head)
  }

  def insert(rows : Seq[A]): Future[Seq[Long]] ={
    db.run(tableQ returning tableQ.map(_.id) ++= rows.filter(_.isValid))
  }

  def update(row : A): Future[Int] = {
    if (row.isValid)
      db.run(tableQ.filter(_.id === row.id).update(row))
    else
      Future{0}
  }

  def update(rows : Seq[A]): Future[Unit] = {
    db.run(DBIO.seq(rows.filter(_.isValid).map(r => tableQ.filter(_.id === r.id).update(r)): _*))
  }

  def findById(id : Long): Future[Option[A]] = {
    db.run(tableQ.filter(_.id === id).result.headOption)
  }

  def findByFilter[C : CanBeQueryCondition](f: (T) => C): Future[Seq[A]] = {
    db.run(tableQ.withFilter(f).result)
  }

  def deleteById(id : Long): Future[Int] = {
    deleteById(Seq(id))
  }

  def deleteById(ids : Seq[Long]): Future[Int] = {
    db.run(tableQ.filter(_.id.inSet(ids)).delete)
  }

  def deleteByFilter[C : CanBeQueryCondition](f:  (T) => C): Future[Int] = {
    db.run(tableQ.withFilter(f).delete)
  }

}