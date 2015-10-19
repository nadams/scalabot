package net.node3.scalabot.plugins.weather

import org.json4s._

object Transforms {
  val weather: PartialFunction[JField, JField] = x => x match {
    case ("temp_max", x) => "maxTemp" -> x
    case ("temp_min", x) => "minTemp" -> x
  }
}
