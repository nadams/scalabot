package net.node3.scalabot

import akka.actor.ActorRef

class BotCommandHandler(val plugins: Seq[Plugin]) {
  def handleMessage(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    plugins.flatMap { plugin =>
      plugin(from, to, message, bot).map { result =>
        if(result == "") Some(result)
        else None
      }.getOrElse(None)
    }

  def handlesMessage(from: MessageSource, to: String, message: String): Boolean =
    plugins.exists(_.handlesMessage(from, to, message))
}

