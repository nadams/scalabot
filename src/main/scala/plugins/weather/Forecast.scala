package net.node3.scalabot.plugins.weather

import com.github.nscala_time.time.Imports._
import org.ocpsoft.prettytime._

case class Forecast(
  val city: ForecastLocation,
  val cod: String,
  val message: Double,
  val cnt: Int,
  val list: Seq[ForecastItem]
) extends Separator {
  private val prettyTime = new PrettyTime()

  val locationString = s"${city.name}"

  override def toString =
    Seq(
      locationString,
      list.zipWithIndex.filter { case (item, index) => index % 8 == 0 }.map(_._1.toString).mkString(separator)
    ).mkString(separator)
}

case class ForecastItem(
  val dt: Long,
  val main: Conditions,
  val weather: Seq[WeatherItem],
  val clouds: Clouds,
  val wind: Wind
) extends Separator {
  val dayOfWeek = new DateTime(dt * 1000L).dayOfWeek.asText

  override def toString =
    s"$dayOfWeek: " +
    Seq(
      main.temperatureString(false),
      weather.foldLeft("")((acc, item) => acc + item.toString),
      main.highLowString(false)
    ).mkString(separator)
}
