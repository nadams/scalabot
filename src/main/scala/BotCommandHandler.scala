package net.node3.scalabot

object BotCommandHandler {
  def handleMessage(from: String, to: String, message: String) : String = "hello"
  def handlesMessage(from: String, to: String, message: String) : Boolean = true
}
