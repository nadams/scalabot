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
      case Array(_, zip, _*) =>
        val messageResponse = Http(url(WeatherPlugin.forecastForZip(zip)) OK as.String).map { x =>
          Some(parse(x).transformField(Transforms.weather).extract[Forecast].toString)
        }.recover {
          case e: Throwable => {
            e.printStackTrace
            None
          }
        }

        Seq(Await.result(messageResponse, WeatherPlugin.timeout).getOrElse("Operation timed out"))
      case _ => Seq.empty
    }

  def currentWeather(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    message.split(" ") match {
      case Array(_, m, _*) =>
        m match {
          case WeatherPlugin.usZipRegex(zip, _*) => currentWeatherForZip(s"$zip,us")
          case _ => Seq.empty
        }
      case _ => Seq.empty
    }

  private def currentWeatherForZip(zip: String): Seq[String] = {
    val messageResponse = Http(url(WeatherPlugin.currentWeatherForZip(zip)) OK as.String).map { x =>
      Some(parse(x).transformField(Transforms.weather).extract[Weather].toString)
    }.recover {
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
  val usZipRegex = """(\d{5})(-\d{4})?""".r

  val currentWeatherEndpoint = s"$api/weather"
  val forecastEndpoint = s"$api/forecast"

  def currentWeatherForZip(zip: String) = s"$currentWeatherEndpoint?zip=$zip&APPID=$apiKey"
  def forecastForZip(zip: String) = s"$forecastEndpoint?zip=$zip&APPID=$apiKey"
}

