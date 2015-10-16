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
          val forecast = parse(x).transformField {
            case ("temp_max", x) => "maxTemp" -> x
            case ("temp_min", x) => "minTemp" -> x
          }.extract[Forecast]

          Some(forecast.toString)
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
        val messageResponse = Http(url(WeatherPlugin.currentWeatherForZip(m)) OK as.String).map { x =>
          val currentWeather = parse(x).transformField {
            case ("temp_max", x) => "maxTemp" -> x
            case ("temp_min", x) => "minTemp" -> x
          }.extract[Weather]

          Some(currentWeather.toString)
        }.recover {
          case e: Throwable => {
            e.printStackTrace
            None
          }
        }

        Seq(Await.result(messageResponse, WeatherPlugin.timeout).getOrElse("Operation timed out"))
      case _ => Seq.empty
    }
}

object WeatherPlugin {
  val config = WeatherConfig(Conf.config.getConfig("bot.weather"))
  val apiKey = config.apiKey
  val timeout = config.timeout.seconds
  val api = "http://api.openweathermap.org/data/2.5"

  val currentWeatherEndpoint = s"$api/weather"
  val forecastEndpoint = s"$api/forecast"

  def currentWeatherForZip(zip: String) = s"$currentWeatherEndpoint?zip=$zip&APPID=$apiKey"
  def forecastForZip(zip: String) = s"$forecastEndpoint?zip=$zip&APPID=$apiKey"
}

