package net.node3.scalabot

import java.net.InetSocketAddress
import akka.actor.ActorSystem

object Main {
  def main(args: Array[String]) : Unit = {
    val system = ActorSystem("irc")
    val server = "172.16.16.4"
    val port = 6667

    val bot = system.actorOf(Bot.props("", "scalabot", "scalabot", "localhost", "scalabot"))
    val irc = system.actorOf(IRC.props(new InetSocketAddress(server, port), bot))
  }
}

