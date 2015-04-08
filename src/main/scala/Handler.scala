package net.node3.scalabot

import scala.annotation.tailrec
import scala.util.Success

import java.net.InetSocketAddress

import akka.actor._
import akka.io.Tcp
import akka.util.{ ByteString, ByteStringBuilder }

import Messages._
import Tokens._

object Handler {
  def props(remote: InetSocketAddress, connection: ActorRef, responder: ActorRef, charset: String, quitMsg: String) =
    Props(classOf[Handler], remote, connection, responder, charset, quitMsg)
}

class Handler(remote: InetSocketAddress, connection: ActorRef, responder: ActorRef, charset: String, quitMsg: String) extends Actor {
  import Tcp._

  var buffer: Option[String] = None

  def out(value: ByteString): ByteString =
    (new ByteStringBuilder ++= value ++= Chars.crlf).result

  def parseMessage(value: String) = PEGParser(value) match {
    case Success(message) => Some(message)
    case _ => None
  }

  @tailrec
  private def respondToInput(in: String) : Unit = buffer match {
    case Some(bufferedInput) =>
      buffer = None
      respondToInput(bufferedInput + in)
    case None =>
      for(message <- parseMessage(in)) {
        responder ! message
      }
  }

  override def preStart() = responder ! Connected

  def receive = {
    case response: Response =>
      response.byteString.utf8String.split(Chars.rn).filterNot(x => x.startsWith("PONG")).foreach(x => println(s"Response: $x"))
      connection ! Write(out(response.byteString))
    case Received(data) =>
      val input = data.decodeString(charset)
      val lines = input.split(Chars.rn)
      lines.filterNot(x => x.startsWith("PING")).foreach { line =>
        println(s"Received: $line")
      }

      handleInput(0)

      @tailrec
      def handleInput(index: Int) : Unit =
        if(index < lines.length - 1) {
          respondToInput(lines(index))
          handleInput(index + 1)
        } else {
          input.takeRight(2) match {
            case Chars.rn => respondToInput(lines(index))
            case _ => buffer = Some(lines(index))
          }
        }
  }
}
