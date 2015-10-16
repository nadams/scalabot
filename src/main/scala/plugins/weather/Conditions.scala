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
  lazy val humidityString = s"Humidity: ${humidity}%"

  def temperatureString(includeCelsius: Boolean = true) = s"Temperature: $tempF°F" + (if(includeCelsius) s" ($tempC°C)" else "")
  def highLowString(includeCelsius: Boolean = true) = s"High/Low: ${maxTempF}°F / ${minTempF}°F" + (if(includeCelsius) s" (${maxTempC}°C / ${minTempC}°C)" else "")
}
