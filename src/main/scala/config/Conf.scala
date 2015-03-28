package net.node3.scalabot.config

import com.typesafe.config.{ Config, ConfigFactory }

object Conf {
  val config = ConfigFactory.load()
  val nick = config.getString("bot.name")
  val dbFile = config.getString("bot.db")
  val migrations = config.getString("bot.migrations")
}

