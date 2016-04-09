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
import org.joda.time.DateTime

import net.node3.scalabot._
import net.node3.scalabot.data._

class UrlTitlePlugin extends Plugin with LazyLogging {
  import net.ruippeixotog.scalascraper.dsl.DSL._
  import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
  import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
  import net.ruippeixotog.scalascraper.util.Validated._

  val timeout = 15.seconds
  val throttle = 2

  private val urlRegex = """(https?:\/\/(?:www\.|(?!www))[^\s\.]+\.[^\s]{2,}|www\.[^\s]+\.[^\s]{2,})""".r
  private val browser = JsoupBrowser()

  private var lastTitle = DateTime.now

  override def handlesMessage(from: MessageSource, to: String, message: String): Boolean = true

  def apply(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    if(lastTitle.plusSeconds(throttle).isBeforeNow)
      urlRegex.findFirstIn(message).map { m =>
        val response = Http.configure(_.setFollowRedirects(true))(url(m) > as.Response(getTitle)).recover {
          case e: TimeoutException => Some(s"Timeout after ${timeout.toString}")
          case e: Throwable => {
            logger.error("Could not get url title", e)

            Some(s"Could not get URL title, try again later...")
          }
        }

        Await.result(response, timeout).map { x =>
          lastTitle = DateTime.now

          Seq(s"$to: URL Title for $m - $x")
        }.getOrElse(Seq.empty)
      }.getOrElse(Seq.empty)
    else Seq.empty

  def getTitle(r: Response): Option[String] =
    (browser.parseString(r.getResponseBody) >?> element("title")).map(x => Some(x.text.trim)).getOrElse(None)
}
