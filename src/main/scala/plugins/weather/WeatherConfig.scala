package net.node3.scalabot.plugins.weather

import com.typesafe.config.Config

case class WeatherConfig(
  val timeout: Int,
  val apiKey: String,
  val commandPrefix: String,
  val separator: String
)

object WeatherConfig {
  def apply(c: Config): WeatherConfig = WeatherConfig(
    if(c.hasPath("timeout")) c.getInt("timeout") else 15,
    c.getString("apiKey"),
    if(c.hasPath("commandPrefix")) c.getString("commandPrefix") else "!",
    if(c.hasPath("separator")) c.getString("separator") else " || "
  )
}
