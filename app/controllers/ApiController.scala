package controllers

import java.sql.Timestamp
import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import models.daos.{GameDAO, OfferDAO}
import models.entities.{Category, Game, Genre, Offer, Platform}
import org.joda.time.{DateTime, Days}
import play.api.libs.json._
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

class ApiController @Inject()(gameDAO: GameDAO, offerDAO: OfferDAO)
                             (implicit ec:ExecutionContext, system: ActorSystem, mat:Materializer) extends Controller {

  implicit val gameWrites = new Writes[Game] {
    def writes(game: Game) = Json.obj(
      "id" -> game.id,
      "name" -> game.name,
      "cover" -> game.cover,
      "publisher" -> game.publisher,
      "developer" -> game.developer,
      "link" -> game.link,
      "description" -> game.description,
      "rating" -> game.rating,
      "releaseDate" -> game.releaseDate.toString,
      "typeGame" -> game.typeGame,
      "videoLink" -> game.videoLink,
      "metacritic" -> game.metacritic
    )
  }

  implicit val offerWrites = new Writes[Offer] {
    def writes(offer: Offer) = Json.obj(
      "id" -> offer.id,
      "idGame" -> offer.idGame,
      "idPlatform" -> offer.idPlatform,
      "link" -> offer.link,
      "idStore" -> offer.idStore,
      "normalPrice" -> offer.normalPrice,
      "offerPrice" -> offer.offerPrice,
      "discount" -> offer.discount,
      "fromDate" -> offer.fromDate,
      "untilDate" -> offer.untilDate.toString
    )
  }

  implicit val platformWrites = new Writes[Platform] {
    def writes(platform: Platform) = Json.obj(
      "id" -> platform.id,
      "name" -> platform.name
    )
  }

  implicit val OfferGamePlatformWrites = new Writes[(Offer, Game, Platform)] {
    override def writes(tuple: (Offer, Game, Platform)) = Json.obj(
      "offer" -> tuple._1,
      "game" -> tuple._2,
      "platform" -> tuple._3
    )
  }

  implicit val genreCountWrites = new Writes[(Genre, Int)] {
    override def writes(tuple: (Genre, Int)) = Json.obj(
      "name" -> tuple._1.name,
      "count" -> tuple._2
    )
  }

  implicit val platformCountWrites = new Writes[(Platform, Int)] {
    override def writes(tuple: (Platform, Int)) = Json.obj(
      "name" -> tuple._1.name,
      "count" -> tuple._2
    )
  }

  implicit val categoriesCountWrites = new Writes[(Category, Int)] {
    override def writes(tuple: (Category, Int)) = Json.obj(
      "name" -> tuple._1.name,
      "count" -> tuple._2
    )
  }

  def games() = Action.async { implicit request =>
    gameDAO.all.map { games =>
      if (games.isEmpty)
        Ok(createErrorJSON("No existen juegos"))
      else
        Ok(createSuccessJSON(Json.toJson(games)))
    }
  }

  def gameById(id : Long) = Action.async{


    gameDAO.findById(id).map{
      case Some(game) =>  {
        Ok(createSuccessJSON(Json.toJson(List(game))))
      }
      case  None => {
        Ok(createErrorJSON("No existe juego con id = "+id))
      }
    }
  }

  def gameByName(name : String) = Action.async{

    gameDAO.searchByName(name).map{ games =>
      if (games.isEmpty)
        Ok(createErrorJSON("No existen juegos"))
      else
        Ok(createSuccessJSON(Json.toJson(games)))
    }

  }

  def filterOffers(pageNumber: Int, platform: String, genre: String, category: String) = Action.async{
    val result = offerDAO.filterOffers(
      pageNumber = pageNumber,
      platformFilter = platform,
      genreFilter = genre,
      categoryFilter = category
    )

    for {
      offers <- result._1
      genresCount <- result._2
      platformCount <- result._3
      categoriesCount <- result._4
      totalOffers <- result._5
    } yield if (offers.isEmpty) Ok(createErrorJSON("No existen ofertas"))
            else Ok(createSuccessJSON(Json.obj(
              "offers" -> Json.toJson(offers),
              // contadores de genero, plataformas y categorias
              "genre" -> Json.toJson(genresCount),
              "platform" -> Json.toJson(platformCount),
              "category" -> Json.toJson(categoriesCount),
              "totalOffers" -> Json.toJson(totalOffers.length))))
  }


  implicit def dateTimeOrdering: Ordering[Timestamp] = Ordering.fromLessThan(_ before  _)

  def numberToMonth(numberMont: Int) = numberMont match {
    case 1 => "Jan"
    case 2 => "Feb"
    case 3 => "Mar"
    case 4 => "Apr"
    case 5 => "May"
    case 6 => "Jun"
    case 7 => "Jul"
    case 8 => "Aug"
    case 9 => "Sep"
    case 10 => "Oct"
    case 11 => "Nov"
    case 12 => "Dic"
    case _ => "Not Found Month"
  }

  implicit val tupleDateWrite = new Writes[DateTime] {
    override def writes(date: DateTime): JsValue = Json.toJson(date.getDayOfMonth + " " + numberToMonth(date.getMonthOfYear) + " " + date.getYear)
  }

  def sameDate(date1: DateTime, date2: DateTime) = {
    date1.getDayOfMonth == date2.getDayOfMonth && date1.getMonthOfYear == date2.getMonthOfYear && date1.getYear == date2.getYear
  }

  def dataForGraphic(idGame: Int) = Action.async{
    gameDAO.dataForGraphic(idGame).map{ offers =>
      if (offers.isEmpty) Ok(createErrorJSON("No existen ofertas"))
      else {
        // obtener los labels del grafico
        val minDate = new DateTime(offers.map(_.fromDate).min)
        val now = DateTime.now()

        val numberOfDays = Days.daysBetween(minDate, now).getDays()
        val days = for (f<- 0 to numberOfDays) yield minDate.plusDays(f)

        val offersByStore = offers.groupBy(_.idStore).map{case (store, offersStore) => (store, offersStore.map(offer => (new DateTime(offer.fromDate), offer.offerPrice)))}

        val points = offersByStore.map{case (store, offersStore) =>
            val dataStore = days.map(day => {
              var ret: Option[Double] = None
              offersStore.foreach((offerDayPrice) => {
                if (sameDate(offerDayPrice._1, day)) ret = Some(offerDayPrice._2)
              })
              ret
            })
            Json.obj(
              "label" -> store,
              "fill" -> false,
              "lineTension" -> 0.1,
              "backgroundColor" -> "rgba(75,192,192,0.4)",
              "borderColor" -> "rgba(75,192,192,1)",
              "borderCapStyle" -> "butt",
              "borderDashOffset" -> 0.0,
              "borderJoinStyle" -> "miter",
              "pointBorderColor" -> "rgba(75,192,192,1)",
              "pointBackgroundColor" -> "#fff",
              "pointBorderWidth" -> 1,
              "pointHoverRadius" -> 5,
              "pointHoverBackgroundColor" -> "rgba(75,192,192,1)",
              "pointHoverBorderColor" -> "rgba(220,220,220,1)",
              "pointHoverBorderWidth" -> 2,
              "pointRadius" -> 10,
              "pointHitRadius" -> 10,
              "data" -> dataStore
            )
        }
        Ok(createSuccessJSON(Json.obj(
          "datasets" -> Json.toJson(points),
          "labels" -> Json.toJson(days)
        )))
        //Ok(createErrorJSON("No existen ofertas"))
      }
    }
  }

  def createSuccessJSON(data : JsValue) = {
    Json.obj(
      "data" -> data,
      "status" -> "success"
    )
  }
  def createErrorJSON( message : String) = {
    Json.obj(
      "message" -> message,
      "status" -> "error"
    )
  }

  def createFailJSON(data: JsValue) = {
    Json.obj(
      "data" -> data,
      "status" -> "fail"
    )
  }
}
