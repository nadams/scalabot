package net.node3.scalabot.config

import scala.collection.JavaConversions._
import scala.collection.immutable.Seq

import com.typesafe.config.Config
import com.github.kxbmap.configs._

object Network {
  def apply(c: Config): Network = Network(
    c.getString("hostname"),
    c.getInt("port"),
    c.opt[String]("password") getOrElse "",
    c.getStringList("channels").to[Seq].map(Channel(_))
  )
}

case class Network(hostname: String, port: Int, password: String, channels: Seq[Channel])
