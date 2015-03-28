package net.node3.scalabot

import scala.collection.JavaConversions._

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import com.github.kxbmap.configs._

import net.node3.scalabot.config._
import net.node3.scalabot.db._

object Main {
  def main(args: Array[String]) : Unit = {
    val system = ActorSystem("irc")
    val networks = Conf.config.getConfigList("bot.networks").map(Network(_))
    val server = networks(0).hostname
    val port = networks(0).port
    val nick = Conf.config.getString("bot.name")
    val realname = Conf.config.getString("bot.realname")

    Migrations.applyMigrations("")

    val bot = system.actorOf(Bot.props("", nick, nick, "localhost", realname))
    val irc = system.actorOf(IRC.props(new InetSocketAddress(server, port), bot))
  }
}

