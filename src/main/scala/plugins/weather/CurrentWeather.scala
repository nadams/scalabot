package net.node3.scalabot.plugins.weather

import com.github.nscala_time.time.Imports._
import org.ocpsoft.prettytime._

case class CurrentWeather(
  val coord: Coordinate,
  val weather: Weather,
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

  override def toString =
    Seq(
      s"$name (${coord.toString})",
      s"Updated: ${timePrettyPrint}",
      s"Conditions: ${weather.main} (${weather.description})",
      s"Temperature: ${main.tempF}°F (${main.tempC}°C)",
      s"High/Low: ${main.maxTempF}°F / ${main.minTempF}°F (${main.maxTempC}°C / ${main.minTempC}°C)",
      s"Humidity: ${main.humidity}%",
      s"Wind: ${wind.deg.toDirection} at ${wind.speed.toMph} Mph (${wind.speed} Km/h)"
    ).mkString(" || ")

  def timePrettyPrint(): String = prettyTime.format(new DateTime(dt * 1000L).date)
}
