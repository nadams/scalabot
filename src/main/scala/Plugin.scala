package net.node3.scalabot

case class MessageSource(source: String, name: Option[String], hostname: Option[String])

trait Plugin {
  def apply(from: MessageSource, to: String, message: String): Option[String]
  def handlesMessage(from: MessageSource, to: String, message: String): Boolean
}
