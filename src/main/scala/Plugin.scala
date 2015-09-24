package net.node3.scalabot

import akka.actor.ActorRef

case class MessageSource(source: String, name: Option[String], hostname: Option[String])

trait Plugin {
  def apply(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String]
  def handlesMessage(from: MessageSource, to: String, message: String): Boolean
}

trait PluginHelper extends Plugin {
  type MessageHandler = (MessageSource, String, String, ActorRef) => Seq[String]
  val messages = Map[String, MessageHandler]()

  override def apply(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    message.split(" ") match {
      case Array(command, _*) => messages.get(command).map(_(from, to, message, bot)).getOrElse(Seq.empty)
      case _ => Seq.empty
    }

  override def handlesMessage(from: MessageSource, to: String, message: String): Boolean =
    messages.keySet.exists(message.toLowerCase.startsWith(_))

  implicit def stringToSeq(s: String): Seq[String] =
    if(s.length == 0) Seq.empty
    else Seq(s)
}
