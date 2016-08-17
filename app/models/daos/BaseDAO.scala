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
class GameDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends BaseDAO[GameTable, Game](dbConfigProvider){
  import dbConfig.driver.api._
  import dbConfig._
  protected val tableQ = SlickTables.gameQ

  def all: Future[Seq[Game]] = {
    db.run(tableQ.result)
  }

  def offersByGame(id :Long): Future[Seq[(Offer, Platform, String)]] = {
    val offerQ = SlickTables.offerQ
    val platformQ = SlickTables.platformQ
    val storeQ = SlickTables.storeQ

    val query = for {
      ((offer, platform), store) <- offerQ join platformQ on (_.idPlatform === _.id) join storeQ on (_._1.idStore === _.id) if offer.idGame === id
    } yield (offer, platform, store.name)

    db.run(query.result)
  }

  def searchByName(name: String): Future[Seq[Game]] = {
    val query = for {
      game <- tableQ if game.name.toLowerCase.like("%" + name.toLowerCase() + "%")
    } yield game
    db.run(query.result)
  }

  def dataForGraphic(id: Long): Future[Seq[(Offer, Store)]] = {
    val offerQ = SlickTables.offerQ
    val storeQ = SlickTables.storeQ

    val query = (offerQ join storeQ on (_.idStore === _.id)).filter(_._1.idGame === id)

    db.run(query.result)
  }
}

@Singleton
class PsStoreDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends BaseDAO[PsStoreTable, PsStore](dbConfigProvider){
  import dbConfig.driver.api._
    import dbConfig._

  protected val tableQ = SlickTables.psStoreQ

  def all: Future[Seq[(String, Long)]] = {
    val query = for {
      p <- tableQ
    } yield (p.idStore, p.idGame)
    db.run(query.result)
  }
}

@Singleton
class GogDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends BaseDAO[GogStoreTable, GogStore](dbConfigProvider){
  import dbConfig.driver.api._
  import dbConfig._

  protected val tableQ = SlickTables.gogStoreQ

  def all: Future[Seq[(String, Long)]] = {
    val query = for {
      p <- tableQ
    } yield (p.idStore, p.idGame)
    db.run(query.result)
  }
}

@Singleton
class SteamDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends BaseDAO[SteamStoreTable, SteamStore](dbConfigProvider){
  import dbConfig.driver.api._
  import dbConfig._

  protected val tableQ = SlickTables.steamStoreQ

  def all: Future[Seq[(String, Long)]] = {
    val query = for {
      p <- tableQ
    } yield (p.idStore, p.idGame)
    db.run(query.result)
  }
}


@Singleton
class OfferDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends BaseDAO[OfferTable, Offer](dbConfigProvider){
  import dbConfig.driver.api._
    import dbConfig._

  protected val tableQ = SlickTables.offerQ

  def all: Future[Seq[Offer]] = {
    db.run(tableQ.result)
  }

  def insertIfNotExists(offerInsert: Offer): Future[Option[Offer]] = {
    val offerInsertAction = tableQ.filter(o => {
      o.link === offerInsert.link &&
      o.idGame === offerInsert.idGame &&
      o.idStore === offerInsert.idStore &&
      o.idPlatform === offerInsert.idPlatform &&
      o.fromDate === offerInsert.fromDate &&
      o.untilDate === offerInsert.untilDate &&
      o.normalPrice === offerInsert.normalPrice &&
      o.offerPrice === offerInsert.offerPrice &&
      o.discount === offerInsert.discount
    }).result.headOption.flatMap {
      case Some(offer) =>
        DBIO.successful(None)
      case None =>
        val offerId =
          (tableQ returning tableQ.map(_.id)) += Offer(
            id = 0,
            link = offerInsert.link,
            idGame = offerInsert.idGame,
            idStore = offerInsert.idStore,
            idPlatform = offerInsert.idPlatform,
            fromDate = offerInsert.fromDate,
            untilDate = offerInsert.untilDate,
            normalPrice = offerInsert.normalPrice,
            offerPrice = offerInsert.offerPrice,
            discount = offerInsert.discount,
            visits = offerInsert.visits
          )

        val offer = offerId.map { id => Offer(
          id = id,
          link = offerInsert.link,
          idGame = offerInsert.idGame,
          idStore = offerInsert.idStore,
          idPlatform = offerInsert.idPlatform,
          fromDate = offerInsert.fromDate,
          untilDate = offerInsert.untilDate,
          normalPrice = offerInsert.normalPrice,
          offerPrice = offerInsert.offerPrice,
          discount = offerInsert.discount,
          visits = offerInsert.visits
        )}
        offer.map(Some(_))
    }.transactionally
    db.run(offerInsertAction)
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

  def filterOffers(
                    pageSize:Int = 8,
                    pageNumber: Int,
                    gameName : String,
                    platformFilter: String,
                    genreFilter: String,
                    categoryFilter: String,
                    sort: String,
                    order: String): (Future[Seq[(Offer, Game, Platform, Store)]],
                                              Future[Seq[(Genre, Int)]],
                                              Future[Seq[(Platform, Int)]],
                                              Future[Seq[(Category, Int)]],
                                              Future[Seq[Long]]) = {

    val gameQ = SlickTables.gameQ
    val platformQ = SlickTables.platformQ
    val gameGenreQ = SlickTables.gameGenreQ
    val genreQ = SlickTables.genreQ
    val gameCategoryQ = SlickTables.gameCategoryQ
    val categoryQ = SlickTables.categoryQ
    val storeQ = SlickTables.storeQ

    val dateNow = new Timestamp(new java.util.Date().getTime)

    val query = for {
      (((((((offer, game), platform), gameGenre), genre), gameCategory), category), store) <- tableQ join gameQ on (_.idGame === _.id) join platformQ on (_._1.idPlatform === _.id) join gameGenreQ on (_._1._2.id === _.idGame) join genreQ on (_._2.idGenre === _.id) join gameCategoryQ on (_._1._1._1._2.id === _.idGame) join categoryQ on (_._2.idCategory === _.id) join storeQ on (_._1._1._1._1._1._1.idStore === _.id)
      if offer.untilDate.>(dateNow) && offer.normalPrice =!= offer.offerPrice
    } yield (offer, game, platform, genre, category, store)

    var queryFilter = query
    if (gameName != "") queryFilter = queryFilter.filter((tuple) => tuple._2.name.toLowerCase like "%"+gameName.toLowerCase+"%")
    if (platformFilter != "") queryFilter = queryFilter.filter((tuple) => tuple._3.name === platformFilter)
    if (genreFilter != "") queryFilter = queryFilter.filter((tuple) => tuple._4.name === genreFilter)
    if (categoryFilter != "") queryFilter = queryFilter.filter((tuple) => tuple._5.name === categoryFilter)

    // query para sacar todas las ofertas
    var queryResult = queryFilter.groupBy((t) => (t._1, t._2, t._3, t._6)).map { case (t, group) =>
      (t._1, t._2, t._3, t._4)
    }

    val offset = (pageNumber - 1) * pageSize

    //query para sacar el conteo de generos, plataformas y categorias
    val queryCountGenres = queryFilter.groupBy(_._4).map { case (genre, group) => (genre, group.map(_._1.id).countDistinct) }
    val queryCountPlatforms = queryFilter.groupBy(_._3).map { case (platform, group) => (platform, group.map(_._1.id).countDistinct) }
    val queryCountCategories = queryFilter.groupBy(_._5).map { case (category, group) => (category, group.map(_._1.id).countDistinct) }

    // ordenar: posibles parametros de sort -> name, priceOffer // order -> asc, desc
    queryResult = if (order == "asc") {
        if (sort == "name") queryResult.sortBy(_._2.name.asc)
        else queryResult.sortBy(_._1.offerPrice.asc)  //filtro es priceOffer
      } else {  // es desc
        if (sort == "name") queryResult.sortBy(_._2.name.desc)
        else queryResult.sortBy(_._1.offerPrice.desc)
      }

    (db.run(queryResult.drop(offset).take(pageSize).result),
      db.run(queryCountGenres.result),
      db.run(queryCountPlatforms.result),
      db.run(queryCountCategories.result),
      db.run(queryResult.map(_._2.id).result))
  }

  def lastGamesWithOffers: Future[Seq[(Offer, Game, Platform, Store)]] = {
    val gameQ = SlickTables.gameQ
    val platformQ = SlickTables.platformQ
    val storeQ = SlickTables.storeQ

    val dateNow = new Timestamp(new java.util.Date().getTime)

    val query = for {
        (((offer, game) , platform), store) <- tableQ join gameQ on (_.idGame === _.id) join platformQ on (_._1.idPlatform === _.id) join storeQ on (_._1._1.idStore === _.id)
      if offer.untilDate.>(dateNow) && offer.normalPrice =!= offer.offerPrice
    } yield (offer, game, platform, store)
    db.run(query.sortBy(_._1.fromDate.desc).take(4).result)
  }

    def gamesWithOffersLessTenThousand : Future[Seq[(Offer, Game, Platform, Store)]] = {
        val gameQ = SlickTables.gameQ
        val platformQ = SlickTables.platformQ
        val storeQ = SlickTables.storeQ

        val dateNow = new Timestamp(new java.util.Date().getTime)

        val query = for {
            (((offer, game) , platform), store) <- tableQ join gameQ on (_.idGame === _.id) join platformQ on (_._1.idPlatform === _.id)  join storeQ on (_._1._1.idStore === _.id)
            if offer.untilDate.>(dateNow) && offer.normalPrice =!= offer.offerPrice && offer.offerPrice.<=(5.toDouble)
        } yield (offer, game, platform, store)
        db.run(query.sortBy(_._1.offerPrice.asc).take(4).result)
    }

    def gamesWithBestOffers: Future[Seq[(Offer, Game, Platform, Store)]] = {
        val gameQ = SlickTables.gameQ
        val platformQ = SlickTables.platformQ
        val storeQ = SlickTables.storeQ

        val dateNow = new Timestamp(new java.util.Date().getTime)

        val query = for {
            (((offer, game) , platform), store) <- tableQ join gameQ on (_.idGame === _.id) join platformQ on (_._1.idPlatform === _.id)  join storeQ on (_._1._1.idStore === _.id)
            if offer.untilDate.>(dateNow) && offer.normalPrice =!= offer.offerPrice
        } yield (offer, game, platform, store)
        db.run(query.sortBy(_._1.discount.desc).take(8).result)
    }

    def gamesWithOffersByEnd : Future[Seq[(Offer, Game, Platform, Store)]] = {
        val gameQ = SlickTables.gameQ
        val platformQ = SlickTables.platformQ
        val storeQ = SlickTables.storeQ

        val dateNow = new Timestamp(new java.util.Date().getTime)

        val query = for {
            (((offer, game) , platform), store) <- tableQ join gameQ on (_.idGame === _.id) join platformQ on (_._1.idPlatform === _.id)  join storeQ on (_._1._1.idStore === _.id)
            if offer.untilDate.>(dateNow) && offer.normalPrice =!= offer.offerPrice
        } yield (offer, game, platform, store)
        db.run(query.sortBy(_._1.untilDate.asc).take(8).result)
    }

    def gamesWithOffersMoreVisits : Future[Seq[(Offer, Game, Platform, Store)]] = {
        val gameQ = SlickTables.gameQ
        val platformQ = SlickTables.platformQ
        val storeQ = SlickTables.storeQ

        val dateNow = new Timestamp(new java.util.Date().getTime)

        val query = for {
            (((offer, game) , platform), store) <- tableQ join gameQ on (_.idGame === _.id) join platformQ on (_._1.idPlatform === _.id)  join storeQ on (_._1._1.idStore === _.id)
            if offer.untilDate.>(dateNow) && offer.normalPrice =!= offer.offerPrice
        } yield (offer, game, platform, store)
        db.run(query.sortBy(_._1.visits.desc).take(4).result)
    }
}

@Singleton
class PlatformDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends BaseDAO[PlatformTable, Platform](dbConfigProvider){
  import dbConfig.driver.api._
    import dbConfig._

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

  def platformsGame(game: Game): Future[Seq[Platform]] = {
    val gamePlatformQ = SlickTables.gamePlatformQ

    val query = for {
      (gamePlatform, platform) <- gamePlatformQ join tableQ on (_.idPlatform === _.id)
      if gamePlatform.idGame === game.id
    } yield platform

    db.run(query.result)
  }
}

@Singleton
class GenreDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends BaseDAO[GenreTable, Genre](dbConfigProvider){
  import dbConfig.driver.api._
    import dbConfig._

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
class CategoryDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends BaseDAO[CategoryTable, Category](dbConfigProvider){
  import dbConfig.driver.api._
    import dbConfig._

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


abstract class BaseDAO[T <: BaseTable[A], A <: BaseEntity] @Inject()(dbConfigProvider: DatabaseConfigProvider) extends AbstractBaseDAO[T,A]{ //with HasDatabaseConfig[JdbcProfile]
//  protected lazy val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig.driver.api._
  import dbConfig._

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