package net.node3.scalabot.plugins

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.xml.XML

import java.util.concurrent.TimeoutException

import akka.actor.ActorRef
import com.ning.http.client.Response
import com.typesafe.scalalogging.LazyLogging
import dispatch._
import dispatch.Defaults._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import net.node3.scalabot._
import net.node3.scalabot.data._

class UrlTitlePlugin extends Plugin with LazyLogging {
  import net.ruippeixotog.scalascraper.dsl.DSL._
  import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
  import net.ruippeixotog.scalascraper.dsl.DSL.Parse._

  val timeout = 15.seconds

  private val urlRegex = """(https?:\/\/(?:www\.|(?!www))[^\s\.]+\.[^\s]{2,}|www\.[^\s]+\.[^\s]{2,})""".r
  private val browser = JsoupBrowser()

  override def handlesMessage(from: MessageSource, to: String, message: String): Boolean = true

  def apply(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    urlRegex.findFirstIn(message).map { m =>
      val response = Http.configure(_.setFollowRedirects(true))(url(m) > as.Response(getTitle)).recover {
        case e: TimeoutException => Some(s"Timeout after ${timeout.toString}")
        case e: Throwable => {
          logger.error("Could not get url title", e)

          Some(s"Could not get URL title, try again later...")
        }
      }

      val title = Await.result(response, timeout)

      Seq(s"$to: URL Title for $m - $title")
    }.getOrElse(Seq.empty)

  def getTitle(r: Response): String =
    browser.parseString(r.getResponseBody).title
}
