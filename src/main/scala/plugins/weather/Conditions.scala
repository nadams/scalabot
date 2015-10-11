package net.node3.scalabot.plugins.weather

case class Conditions(
  val temp: Double,
  val pressure: Double,
  val humidity: Int,
  val minTemp: Double,
  val maxTemp: Double
) {
  lazy val tempC = temp.celsius
  lazy val tempF = temp.fahrenheit

  lazy val minTempC = minTemp.celsius
  lazy val minTempF = minTemp.fahrenheit

  lazy val maxTempC = maxTemp.celsius
  lazy val maxTempF = maxTemp.fahrenheit
}
