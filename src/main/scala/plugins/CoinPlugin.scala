package net.node3.scalabot.plugins

import scala.collection.immutable.Map
import scala.util.Random

import akka.actor.ActorRef

import net.node3.scalabot._

class CoinPlugin extends Plugin with PluginHelper {
  override val messages = Map[String, MessageHandler]("coin" -> flipCoin)

  def flipCoin(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    if(Random.nextBoolean) Seq("Heads")
    else Seq("Tails")
}

