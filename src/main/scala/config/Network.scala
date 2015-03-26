package net.node3.scalabot.config

import com.typesafe.config.Config
import com.github.kxbmap.configs._

object Network {
  def apply(c: Config): Network = Network(c.getString("hostname"), c.getInt("port"), c.opt[String]("password") getOrElse "")
}

case class Network(hostname: String, port: Int = 6667, password: String = "")
