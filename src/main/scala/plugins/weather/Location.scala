package net.node3.scalabot.plugins.weather

case class Location(
  val `type`: Option[Int],
  val id: Option[Int],
  val message: Double,
  val country: String,
  val sunrise: Long,
  val sunset: Long
)

case class ForecastLocation(
  val id: Option[Int],
  val name: String,
  val coord: Coordinate,
  val country: String,
  val population: Option[Int]
)

