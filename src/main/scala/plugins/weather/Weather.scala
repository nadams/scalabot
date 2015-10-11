package net.node3.scalabot.plugins.weather

case class Weather(
  val id: Int,
  val main: String,
  val description: String,
  val icon: String
)
