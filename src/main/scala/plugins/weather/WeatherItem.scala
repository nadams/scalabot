package net.node3.scalabot.plugins.weather

case class WeatherItem(
  val id: Int,
  val main: String,
  val description: String,
  val icon: String
) {
  override def toString = s"${main} ($description)"
}

