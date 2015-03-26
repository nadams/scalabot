package net.node3.scalabot

import scala.collection.immutable._

import akka.actor.{ Actor, Props }
import akka.io.Tcp
import akka.util.ByteString
import Messages._

object Bot {
  def props(password: String, nick: String, username: String, servername: String, realname: String, plugins: Seq[Plugin] = Seq.empty[Plugin]) =
    Props(classOf[Bot], password, nick, username, servername, realname, plugins)
}

case class Bot(password: String, nick: String, username: String, servername: String, realname: String, plugins: Seq[Plugin] = Seq.empty[Plugin]) extends Actor {
  import Tcp._

  val hostname = java.net.InetAddress.getLocalHost.getHostName

  def receive = {
    case Ping(from) =>
      sender ! Pong(from)
    case Connected if !password.isEmpty =>
      sender ! (Pass(password) + Nick(nick) + User(username, hostname, servername, realname))
    case Connected if password.isEmpty =>
      sender ! (Nick(nick) + User(username, hostname, servername, realname))
  }
}
