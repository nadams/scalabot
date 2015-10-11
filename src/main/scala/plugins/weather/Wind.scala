package net.node3.scalabot.plugins.weather

case class Wind(
  val speed: Double,
  val deg: Double,
  val gust: Option[Double]
)

