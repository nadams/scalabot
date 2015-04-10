package net.node3.scalabot

import akka.actor.ActorRef

case class MessageSource(source: String, name: Option[String], hostname: Option[String])

trait Plugin {
  def apply(from: MessageSource, to: String, message: String, bot: ActorRef): Option[String]
  def handlesMessage(from: MessageSource, to: String, message: String): Boolean
}

trait PluginHelper extends Plugin {
  type MessageHandler = (MessageSource, String, String, ActorRef) => Option[String]
  val messages = Map[String, MessageHandler]()

  override def apply(from: MessageSource, to: String, message: String, bot: ActorRef): Option[String] =
    message.split(" ") match {
      case Array(command, _*) => messages.get(command).map(_(from, to, message, bot)).getOrElse(None)
      case _ => None
    }

  override def handlesMessage(from: MessageSource, to: String, message: String): Boolean =
    messages.keySet.exists(message.toLowerCase.startsWith(_))
}
