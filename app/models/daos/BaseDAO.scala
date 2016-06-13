package models.daos

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
    val query = for {
      ((offer, game) , platform) <- tableQ join gameQ on (_.idGame === _.id) join platformQ on (_._1.idPlatform === _.id)
    } yield (offer, game, platform)
    db.run(query.result)
  }
}

@Singleton
class PlatformDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends BaseDAO[PlatformTable, Platform]{
  import dbConfig.driver.api._

  protected val tableQ = SlickTables.platformQ

  def all: Future[Seq[Platform]] = {
    db.run(tableQ.result)
  }

  def allPlatformsWithCount: Future[Seq[(Platform, Int)]] = {
    val gamePlatformQ = SlickTables.gamePlatformQ

    val platformsWithGames = (for {
      (platform, gamePlatform) <- tableQ join gamePlatformQ on (_.id === _.idPlatform)
    } yield (platform, gamePlatform))
      .groupBy(_._1).map {
      case (plat, gamePlat) => (plat, gamePlat.length)
    }

    val platformsWithoutGames = for {
    platform <- tableQ if !platformsWithGames.filter(_._1.id === platform.id).exists
    } yield (platform, 0)

    val query = (platformsWithGames union platformsWithoutGames) sortBy(_._1.name.asc)
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

  def allGenresWithCount: Future[Seq[(Genre, Int)]] = {
    val gameGenreQ = SlickTables.gameGenreQ

    val genresWithGames = (for {
      (genre, gameGenre) <- tableQ join gameGenreQ on (_.id === _.idGenre)
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