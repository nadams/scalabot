package net.node3.scalabot.plugins.weather

import com.github.nscala_time.time.Imports._
import org.ocpsoft.prettytime._

import net.node3.scalabot.plugins.WeatherPlugin

case class Weather(
  val coord: Coordinate,
  val weather: Seq[WeatherItem],
  val base: String,
  val main: Conditions,
  val wind: Wind,
  val clouds: Clouds,
  val dt: Long,
  val sys: Location,
  val id: Int,
  val name: String,
  val cod: Int
) {
  private val prettyTime = new PrettyTime()

  lazy val separator = WeatherPlugin.config.separator
  lazy val periodString = prettyTime.format(new DateTime(dt * 1000L).date)
  lazy val dayOfWeek = new DateTime(dt * 1000L).dayOfWeek.name
  lazy val updatedString = s"Updated: ${periodString}"
  lazy val locationString = s"$name"

  override def toString =
    Seq(
      locationString,
      updatedString,
      "Conditions:" + weather.foldLeft(" ")((acc, item) => acc + item.toString),
      main.temperatureString(),
      main.highLowString(),
      main.humidityString,
      wind.toString
    ).mkString(separator)
}
