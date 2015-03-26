package net.node3.scalabot

trait Plugin {
  def apply(value: String) : Option[String]
}
