package net.node3.scalabot.plugins.weather

case class Coordinate(val lon: Double, val lat: Double) {
  override def toString() = s"$lon/$lat"
}

