package net.node3.scalabot.plugins.weather

import com.typesafe.config.Config

case class WeatherConfig(val timeout: Int, val apiKey: String, val commandPrefix: String)

object WeatherConfig {
  def apply(c: Config): WeatherConfig = WeatherConfig(
    c.getInt("timeout"),
    c.getString("apiKey"),
    c.getString("commandPrefix")
  )
}
