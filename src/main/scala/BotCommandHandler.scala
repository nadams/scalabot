package net.node3.scalabot

import akka.actor.ActorRef

class BotCommandHandler(val plugins: Seq[Plugin]) {
  def handleMessage(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] = plugins.flatMap(_(from, to, message, bot))
  def handlesMessage(from: MessageSource, to: String, message: String): Boolean = plugins.exists(_.handlesMessage(from, to, message))
}

