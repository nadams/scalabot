package net.node3.scalabot.config

import com.typesafe.config.Config

object Network {
  def apply(c: Config): Network = Network(c.getString("hostname"), c.getInt("port"))
}

case class Network(hostname: String, port: Int)
