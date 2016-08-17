package scraping


case class SteamScraping(url: String) {
  import net.ruippeixotog.scalascraper.browser.JsoupBrowser
  import net.ruippeixotog.scalascraper.dsl.DSL._
  import net.ruippeixotog.scalascraper.dsl.DSL.Extract._

  private val doc = JsoupBrowser().get(url)

  private def isValid(id: String, fromDateStr: String, untilDateStr: String, discount: Int, offerPrice: Int) = {
    id != "" && fromDateStr != "" && untilDateStr != "" && discount != 0 && offerPrice != -1
  }

  def offersWithDiscount(): List[DataSteamDb] = {
    val items = doc >> elementList(".appimg")
    items.map(item => {
      val tds = item >> elementList("td")
      val id = item.attrs.get("data-appid") match {
        case Some(id) => id;
        case None => item.attrs.get("data-subid") match {
          case Some(id) => id
          case None => ""
        }
      }
      val link = tds.head >> attr("href")("a")
      val untilDateStr = tds(2) >> attr("title")("span")
      val fromDateStr = tds(5).attrs.get("title") match {case Some(po) => po; case None => ""}
      val discount = tds(3).attrs.get("data-sort") match {case Some(p) => p.toInt; case None => 0}
      val offerPrice = tds(4).attrs.get("data-sort") match {case Some(po) => po.toInt; case None => -1}

      if (isValid(id, fromDateStr, untilDateStr, discount, offerPrice)){
        val offer = DataSteamDb(id, link, untilDateStr, fromDateStr, discount, offerPrice/100.0)
        Some(offer)
      } else None
    }).filter(_.nonEmpty).map(_.get)
  }
}
