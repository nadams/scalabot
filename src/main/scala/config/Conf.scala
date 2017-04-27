package net.node3.scalabot.config

import scala.util.{ Failure, Success, Try }

import com.typesafe.config.{ Config, ConfigFactory }
import org.apache.commons.lang.StringUtils

object Conf {
  val config = ConfigFactory.load()

  val nick = config.getString("bot.name")
  val plugins = config.getStringList("bot.plugins")

  val dbDriver = config.getString("bot.db.driver")
  val dbURL = config.getString("bot.db.url")

  def dbDir = {
    val dir = config.getString("bot.db.migrations")

    if(StringUtils.isEmpty(dir)) "src/universal/db"
    else dir
  }
}

