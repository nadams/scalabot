package net.node3.scalabot

import scala.collection.immutable._

import akka.actor.{ Actor, Props }
import akka.io.Tcp
import akka.util.ByteString

import net.node3.scalabot.config.Channel

import Messages._

object Bot {
  def props(
    password: String,
    nick: String,
    username: String,
    servername: String,
    realname: String,
    channels: Seq[Channel] = Seq.empty[Channel],
    plugins: Seq[Plugin] = Seq.empty[Plugin]
  ) = Props(classOf[Bot], password, nick, username, servername, realname, channels, plugins)
}

case class Bot(
  password: String,
  nick: String,
  username: String,
  servername: String,
  realname: String,
  channels: Seq[Channel] = Seq.empty[Channel],
  plugins: Seq[Plugin] = Seq.empty[Plugin]
) extends Actor {
  import com.github.kxbmap.configs._
  import net.node3.scalabot.config._
  import Tcp._

  val hostname = java.net.InetAddress.getLocalHost.getHostName
  val conf = Conf.config
  implicit val botCommandHandler = new BotCommandHandler(plugins)

  private var hasJoined = false

  def receive = {
    case Connected =>
      if(password.isEmpty) sender ! (Nick(nick) + User(username, hostname, servername, realname))
      else sender ! (Pass(password) + Nick(nick) + User(username, hostname, servername, realname))
    case Ping(from) =>
      sender ! Pong(from)
    case NotRegistered(_) =>
      for(pass <- conf.opt[String]("bot.nickservPass"); email <- conf.opt[String]("bot.email")) {
        sender ! RegisterToNickServ(email, pass)
      }
    case Mode(modes) =>
      if(!hasJoined) {
        sender ! JoinChannelsCommand(channels.map(_.name))
        hasJoined = true
      }
    case NickAlreadyRegistered(_) =>
      sender ! IdentifyToNickServ(conf.get[String]("bot.nickservPass"))
    case BotCommand(source, to, message) =>
      if(to.startsWith("#")) botCommandHandler.handleMessage(MessageSource(to, None, None), source.source, message, sender).foreach { response =>
        sender ! BotCommand(to, response)
      } else botCommandHandler.handleMessage(source, to, message, sender).foreach { response =>
        sender ! BotCommand(source.source, response)
      }
    case PluginMessage(message) =>
      sender ! message
  }
}
