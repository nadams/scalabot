package net.node3.scalabot

import scala.collection.immutable.Seq
import scala.collection.JavaConversions._

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import com.github.kxbmap.configs._
import org.joda.time.DateTimeZone

import net.node3.scalabot.config._
import net.node3.scalabot.db.migrate._
import net.node3.scalabot.plugins._

object Main {
  def main(args: Array[String]) : Unit = {
    DateTimeZone.setDefault(DateTimeZone.UTC)

    try {
      MigrationSystem.applyMigrations(Conf.migrations)
    } catch {
      case e: Throwable =>
        println("Could not apply migrations, shutting down")
        println(e)
        System.exit(1)
    }

    val system = ActorSystem("irc")
    val networks = Conf.config.getConfigList("bot.networks").map(Network(_))
    val nick = Conf.config.getString("bot.name")
    val realname = Conf.config.getString("bot.realname")
    val plugins = loadPlugins()

    val bots = networks.map { network =>
      val bot = system.actorOf(Bot.props("", nick, nick, "localhost", realname, network.channels, plugins))
      val irc = system.actorOf(IRC.props(new InetSocketAddress(network.hostname, network.port), bot))
      bot
    }
  }

  def loadPlugins() : Seq[Plugin] = {
    Conf.plugins.flatMap { name =>
      try {
        Some(Class.forName(name).newInstance.asInstanceOf[Plugin])
      } catch {
        case _: Throwable => {
          println(s"Could not load plugin: $name")
          None
        }
      }
    }.to[Seq]
  }
}

