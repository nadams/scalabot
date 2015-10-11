package net.node3.scalabot.plugins.weather

case class Location(
  val `type`: Int,
  val id: Int,
  val message: Double,
  val country: String,
  val sunrise: Long,
  val sunset: Long
)

