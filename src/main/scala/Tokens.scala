package net.node3.scalabot

import scala.annotation.tailrec
import akka.util.{ ByteString, ByteStringBuilder }

object Tokens {
  sealed trait Token {
    val byteString: ByteString
  }

  sealed trait SimpleToken extends Token {
    val value: String
    lazy val byteString = ByteString(value)
  }

  case class Channel(value: String) extends SimpleToken
  case class UserMask(value: String) extends SimpleToken
  case class Command(value: String) extends SimpleToken
  case class Prefix(target: String, user: Option[String], host: Option[String]) extends Token {
    lazy val byteString = ByteString(List(target, user.getOrElse(""), host.getOrElse("")).mkString(" "))
  }

  case class Message(prefix: Option[Prefix], command: Command, params: List[String]) extends Token with Response {
    def +(r: Response) = new ResponseCollection(List(this, r))
    def mkPrefixByteString(prefix: Option[Prefix]) = prefix.map { x => x.byteString } getOrElse ByteString("")
    def mkParamsString(params: List[String]) = {
      @tailrec
      def inner(ps: List[String], acc: String): String = ps match {
        case x :: y :: xs =>
          inner(xs, acc + " " + x + " " + y)
        case x :: Nil =>
          acc + " :" + x
        case Nil =>
          acc
      }

      params match {
        case Nil => ""
        case x :: xs => inner(xs, x)
      }
    }

    lazy val byteString = (
      new ByteStringBuilder ++=
      mkPrefixByteString(prefix) ++=
      command.byteString ++=
      Chars.space ++=
      ByteString(mkParamsString(params))
    ).result
  }
}
