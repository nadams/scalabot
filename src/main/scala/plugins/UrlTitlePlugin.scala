package net.node3.scalabot.plugins

import scala.concurrent.Await
import scala.concurrent.duration._

import java.util.concurrent.TimeoutException

import akka.actor.ActorRef
import com.ning.http.client.Response
import dispatch._
import dispatch.Defaults._
import org.apache.commons.validator.routines.UrlValidator

import net.node3.scalabot._
import net.node3.scalabot.data._

class UrlTitlePlugin extends Plugin {
  val timeout = 15.seconds

  private val titlePattern = """(?i)<title>(.+)<\/title>""".r
  private val urlValidator = new UrlValidator

  override def handlesMessage(from: MessageSource, to: String, message: String) =
    urlValidator.isValid(message)

  def apply(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] = {
    val response = Http.configure(_.setFollowRedirects(true))(url(message) > as.Response(handleResponse)).map(r => r).recover {
      case _: TimeoutException => Some(s"Timeout after ${timeout.toString}")
      case _: Throwable => Some(s"Couldn't connect, try again later...")
    }

    val title = Await.result(response, timeout).getOrElse("No title found")

    Seq(s"$to: URL Title for $message - $title")
  }

  def handleResponse(r: Response): Option[String] =
    titlePattern.findFirstMatchIn(r.getResponseBody)map(_.group(1))
}
