package net.node3.scalabot

class BotCommandHandler(val plugins: Seq[Plugin]) {
  def handleMessage(from: MessageSource, to: String, message: String): Seq[String] = plugins.flatMap(_(from, to, message))
  def handlesMessage(from: MessageSource, to: String, message: String): Boolean = plugins.exists(_.handlesMessage(from, to, message))
}

