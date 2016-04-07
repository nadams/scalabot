package net.node3.scalabot.config

import com.typesafe.config.{ Config, ConfigFactory }
import org.apache.commons.lang.StringUtils

object Conf {
  val config = ConfigFactory.load()
  val nick = config.getString("bot.name")
  val dbFile = config.getString("bot.db")
  val plugins = config.getStringList("bot.plugins")

  def dbDir = {
    val dir = System.getProperty("db.dir")

    if(StringUtils.isEmpty(dir)) "src/universal/db"
    else dir
  }
}

