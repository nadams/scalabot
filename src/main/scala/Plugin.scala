package net.node3.scalabot

trait Plugin {
  def apply(from: String, to: String, message: String): Option[String]
  def handlesMessage(from: String, to: String, message: String): Boolean
}
