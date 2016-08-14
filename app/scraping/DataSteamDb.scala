package scraping

import java.sql.Timestamp
import org.joda.time.DateTime

case class DataSteamDb(id: String, link: String, fromDateStr: String, untilDateStr: String, discount: Int, offerPrice: Double) {

  implicit def strToTimestamp(s: String) = {
    // 2016-08-08T17:15:10+00:00 <- ver con detalle en caso de fallas de hora
    val date = (s split "T")(0) split "-"
    val time = (s split "T") (1).split("\\+")(0) split ":"
    new Timestamp(new DateTime(date(0).toInt, date(1).toInt, date(2).toInt, time(0).toInt, time(1).toInt).getMillis)
  }

  val fromDate: Timestamp = fromDateStr
  val untilDate: Timestamp = untilDateStr
  val normalPrice: Double = {
    val ret = (offerPrice * 100) / (100 - discount)
    BigDecimal(ret).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  def isValid = {
    id != "" && fromDateStr != "" && untilDateStr != "" && discount != 0 && offerPrice != -1
  }
}
