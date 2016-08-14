package scraping

/**
  * Created by edward on 14-08-16.
  */
case class SteamScraping(url: String) {
  import java.sql.Timestamp
  import net.ruippeixotog.scalascraper.browser.JsoupBrowser
  import net.ruippeixotog.scalascraper.dsl.DSL._
  import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
  import org.joda.time.DateTime

  // representacion de datos
  case class DataSteamDb(id: Int, link: String, fromDateStr: String, untilDateStr: String, discount: Int, offerPrice: Double) {
    def strToTimestamp(s: String) = {
      // 2016-08-08T17:15:10+00:00 <- ver con detalle en caso de fallas de hora
      val date = (s split "T")(0) split "-"
      val time = (s split "T") (1).split("\\+")(0) split ":"
      new Timestamp(new DateTime(date(0).toInt, date(1).toInt, date(2).toInt, time(0).toInt, time(1).toInt).getMillis)
    }

    val fromDate: Timestamp = strToTimestamp(fromDateStr)
    val untilDate = strToTimestamp(untilDateStr)
    val normalPrice: Double = {
      val ret = (offerPrice * 100) / (100 - discount)
      BigDecimal(ret).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    }
  }

  private val doc = JsoupBrowser().get(url)

  private def isValid(id: Int, fromDateStr: String, untilDateStr: String, discount: Int, offerPrice: Int) = {
    id != -1 && fromDateStr != "" && untilDateStr != "" && discount != 0 && offerPrice != -1
  }

  def offersWithDiscount(): List[DataSteamDb] = {
    println("bla")
    val items = doc >> elementList(".appimg")
    items.map(item => {
      val tds = item >> elementList("td")
      val id = item.attrs.get("data-appid") match {case Some(id) => id.toInt; case None => -1}
      val link = tds.head >> attr("href")("a")
      val untilDateStr = tds(2) >> attr("title")("span")
      val fromDateStr = tds(5).attrs.get("title") match {case Some(po) => po; case None => ""}
      val discount = tds(3).attrs.get("data-sort") match {case Some(p) => p.toInt; case None => 0}
      val offerPrice = tds(4).attrs.get("data-sort") match {case Some(po) => po.toInt; case None => -1}
      if (isValid(id, fromDateStr, untilDateStr, discount, offerPrice)){
        Some(DataSteamDb(id, link, untilDateStr, fromDateStr, discount, offerPrice/100.0))
      } else None
    }).filter(_.nonEmpty).map(_.get)
  }
}
