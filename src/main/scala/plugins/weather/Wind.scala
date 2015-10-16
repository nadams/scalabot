package net.node3.scalabot.plugins.weather

case class Wind(
  val speed: Double,
  val deg: Double,
  val gust: Option[Double]
) {
  override def toString = s"${deg.toDirection} at ${speed.toMph} MPH (${speed} km/h)"
}

