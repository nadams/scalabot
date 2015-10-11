package net.node3.scalabot.plugins

import scala.math.BigDecimal
import scala.math.BigDecimal.RoundingMode._

package object weather {
  implicit class KelvinConversion(val d: Double) extends AnyVal {
    def celsius = (d - 273.15).twoPlaces
    def fahrenheit = (celsius * 9 / 5 + 32).twoPlaces
  }

  implicit class MetricConversion(val d: Double) extends AnyVal {
    def toMph = BigDecimal(d * 0.621371).setScale(2, HALF_UP).toDouble
  }

  implicit class RichDouble(val d: Double) extends AnyVal {
    def between(left: Double, right: Double): Boolean = d > left && d <= right
    def twoPlaces = BigDecimal(d).setScale(2, HALF_UP).toDouble
  }

  implicit class DegreeToDirection(val d: Double) extends AnyVal {
    def toDirection: String =
      if(d.between(348.75, 360.0) || d.between(0, 11.25)) "N"
      else if (d.between(11.25, 33.75)) "NNE"
      else if (d.between(33.75, 56.25)) "NE"
      else if (d.between(56.25, 78.75)) "ENE"
      else if (d.between(78.75, 101.25)) "E"
      else if (d.between(101.25, 123.75)) "ESE"
      else if (d.between(123.75, 146.25)) "SE"
      else if (d.between(146.25, 168.75)) "SSE"
      else if (d.between(168.75, 191.25)) "S"
      else if (d.between(191.25, 213.75)) "SSW"
      else if (d.between(213.75, 236.25)) "SW"
      else if (d.between(236.25, 258.75)) "WSW"
      else if (d.between(258.75, 281.25)) "W"
      else if (d.between(281.25, 303.75)) "WNW"
      else if (d.between(303.75, 326.25)) "NW"
      else if (d.between(326.25, 348.75)) "NNW"
      else ""
  }
}
