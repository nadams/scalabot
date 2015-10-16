package net.node3.scalabot.plugins.weather

case class Location(
  val `type`: Int,
  val id: Int,
  val message: Double,
  val country: String,
  val sunrise: Long,
  val sunset: Long
)

case class ForecastLocation(
  val id: Int,
  val name: String,
  val coord: Coordinate,
  val country: String,
  val population: Int
)

