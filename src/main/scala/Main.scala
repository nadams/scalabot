package net.node3.scalabot

import scala.collection.immutable.Seq
import scala.collection.JavaConversions._

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import com.github.kxbmap.configs._
import org.joda.time.DateTimeZone

import net.node3.scalabot.config._
import net.node3.scalabot.db.migrate._
import net.node3.scalabot.account._

object Main {
  def main(args: Array[String]) : Unit = {
    DateTimeZone.setDefault(DateTimeZone.UTC)
    val system = ActorSystem("irc")
    val networks = Conf.config.getConfigList("bot.networks").map(Network(_))
    val server = networks(0).hostname
    val port = networks(0).port
    val nick = Conf.config.getString("bot.name")
    val realname = Conf.config.getString("bot.realname")
    val plugins = Seq[Plugin](new AccountPlugin())

    MigrationSystem.applyMigrations(Conf.migrations)

    val bot = system.actorOf(Bot.props("", nick, nick, "localhost", realname, plugins))
    val irc = system.actorOf(IRC.props(new InetSocketAddress(server, port), bot))
  }
}

