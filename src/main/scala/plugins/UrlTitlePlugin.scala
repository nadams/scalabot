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
import org.apache.commons.codec.net.URLCodec

import net.node3.scalabot._
import net.node3.scalabot.data._

class UrlTitlePlugin extends Plugin with LazyLogging {
  import net.ruippeixotog.scalascraper.dsl.DSL._
  import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
  import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
  import net.ruippeixotog.scalascraper.util.Validated._

  type Validations = Map[String, Boolean]

  val timeout = 15.seconds
  val maxSize = 1024 * 1024 * 2
  val throttle = 2

  private val urlRegex = """(https?:\/\/(?:www\.|(?!www))[^\s\.]+\.[^\s]{2,}|www\.[^\s]+\.[^\s]{2,})""".r
  private val magnetRegex = """(magnet:.+)""".r
  private val browser = JsoupBrowser()
  private val urlCodec = new URLCodec()
  private def client = Http.configure(_.setFollowRedirects(true))
  private val defaultRecover: PartialFunction[Throwable, Option[String]] = {
    case e: TimeoutException => Some(s"Timeout after ${timeout.toString}")
    case e: Throwable => {
      logger.error("Could not get url title", e)

      Some(s"Could not get URL title, try again later...")
    }
  }

  private var lastTitle = DateTime.now.minusSeconds(throttle)

  override def handlesMessage(from: MessageSource, to: String, message: String): Boolean = true

  def apply(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    if(lastTitle.plusSeconds(throttle).isBeforeNow)
      message match {
        case urlRegex(m) => urlTitle(to, m)
        case magnetRegex(m) => magnetTitle(m)
        case _ => Seq.empty
      }
    else Seq.empty

  private def magnetTitle(m: String): Seq[String] = {
    m.replaceFirst("""^magnet:\?""", "")
      .split("&")
      .map(_.split("="))
      .filter(_.length >= 2)
      .map(x => x(0) -> x(1)).toMap
      .get("dn")
      .map(x => Seq(s"Magnet Link Title: ${urlCodec.decode(x)}"))
      .getOrElse(Seq.empty)
  }

  private def urlTitle(to: String, m: String): Seq[String] = {
    val isValidRequest = Await.result(client(url(m).HEAD > as.Response(isHtml)).recover {
      case e: Throwable => Map("failed" -> false)
    }, timeout).forall(x => x._2)

    if(isValidRequest)
      Await.result(client(url(m) > as.Response(getTitle)).recover(defaultRecover), timeout).map { x =>
        lastTitle = DateTime.now

        Seq(s"$to: URL Title for $m - $x")
      }.getOrElse(Seq.empty)
    else Seq.empty
  }

  private def isHtml(r: Response): Validations = Map(
    "content-type" -> r.getContentType.toLowerCase.startsWith("text/html"),
    "content-length" -> ((r.getHeaders.isDefinedAt("Content-Length") && (Integer.parseInt(r.getHeader("Content-Length")) <= maxSize)) || true)
  )

  private def getTitle(r: Response): Option[String] =
    if(r.getContentType.toLowerCase.startsWith("text/html"))
      (browser.parseString(r.getResponseBody) >?> element("title")).map(x => Some(x.text.trim)).getOrElse(None)
    else None
}
