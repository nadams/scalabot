package net.node3.scalabot

class BotCommandHandler(val plugins: Seq[Plugin]) {
  def handleMessage(from: String, to: String, message: String): Seq[String] = plugins.flatMap(_(from, to, message))
  def handlesMessage(from: String, to: String, message: String): Boolean = true
}
