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
  override val messages = Seq[(String, MessageHandler)](
    "w" -> currentWeather,
    "weather" -> currentWeather
  ).map(x => (WeatherPlugin.config.commandPrefix + x._1) -> x._2).toMap

  def currentWeather(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    message.split(" ") match {
      case Array(_, m) =>
        val messageResponse = Http(url(WeatherPlugin.currentWeatherForZip(m)) OK as.String).map { x =>
          implicit val formats = DefaultFormats
          val currentWeather: CurrentWeather = parse(x).transformField {
            case ("temp_max", x) => ("maxTemp", x)
            case ("temp_min", x) => ("minTemp", x)
          }.extract[CurrentWeather]

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
  def currentWeatherForZip(zip: String) = s"$currentWeatherEndpoint?zip=$zip&APPID=$apiKey"
}

