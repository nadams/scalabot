package net.node3.scalabot.plugins

import scala.concurrent.Await
import scala.concurrent.duration._

import java.util.concurrent.TimeoutException

import akka.actor.ActorRef
import com.ning.http.client.Response
import com.typesafe.config.Config
import dispatch._
import dispatch.Defaults._
import org.json4s._
import org.json4s.native.JsonMethods._

import net.node3.scalabot._
import net.node3.scalabot.data._
import net.node3.scalabot.plugins.weather._
import net.node3.scalabot.config.Conf

class WeatherPlugin extends Plugin with PluginHelper {
  implicit val formats = DefaultFormats

  override val messages = Seq[(String, MessageHandler)](
    "w" -> currentWeather,
    "weather" -> currentWeather,
    "forecast" -> forecast,
    "fuckcast" -> forecast
  ).map(x => (WeatherPlugin.config.commandPrefix + x._1) -> x._2).toMap

  def forecast(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    message.split(" ") match {
      case Array(_, m, _*) =>
        m match {
          case WeatherPlugin.usZipRegex(zip, _*) => call(WeatherPlugin.forecastForZip(s"$zip"))(mapForecast)
          case WeatherPlugin.postalCodeRegex(code, _*) => call(WeatherPlugin.forecastForZip(s"$code"))(mapForecast)
          case _ => call(WeatherPlugin.forecastForCity(m))(mapForecast)
        }
      case _ => Seq.empty
    }

  def currentWeather(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =

    message.split(" ") match {
      case Array(_, m, _*) =>
        m match {
          case WeatherPlugin.usZipRegex(zip, _*) => call(WeatherPlugin.currentWeatherForZip(s"$zip"))(mapWeather)
          case WeatherPlugin.postalCodeRegex(code, _*) => call(WeatherPlugin.currentWeatherForZip(s"$code"))(mapWeather)
          case _ => call(WeatherPlugin.currentWeatherForCity(m))(mapWeather)
        }
      case _ => Seq.empty
    }

  def mapWeather(body: String) =
    Some(parse(body).transformField(Transforms.weather).extract[Weather].toString)

  def mapForecast(body: String) =
    Some(parse(body).transformField(Transforms.weather).extract[Forecast].toString)

  private def call(endpoint: String)(action: String => Option[String]): Seq[String] = {
    val messageResponse = Http(url(endpoint) OK as.String).map(action).recover {
      case e: Throwable => {
        e.printStackTrace
        None
      }
    }

    Seq(Await.result(messageResponse, WeatherPlugin.timeout).getOrElse("Operation timed out"))
  }
}

object WeatherPlugin {
  val config = WeatherConfig(Conf.config.getConfig("bot.weather"))
  val apiKey = config.apiKey
  val timeout = config.timeout.seconds
  val api = "http://api.openweathermap.org/data/2.5"
  val usZipRegex = """(\d{5})(-\d{4})?()""".r
  val postalCodeRegex = """[a-zA-Z]\d[a-z-A-Z]\s?\d[a-zA-Z]\d""".r

  val currentWeatherEndpoint = s"$api/weather"
  val forecastEndpoint = s"$api/forecast"

  def currentWeatherForZip(zip: String, country: String = "us") = s"$currentWeatherEndpoint?zip=$zip,$country&APPID=$apiKey"
  def forecastForZip(zip: String, country: String = "us") = s"$forecastEndpoint?zip=$zip,$country&APPID=$apiKey"
  def currentWeatherForCity(q: String) = s"$currentWeatherEndpoint?q=$q&APPID=$apiKey"
  def forecastForCity(q: String) = s"$forecastEndpoint?q=$q&APPID=$apiKey"
}

