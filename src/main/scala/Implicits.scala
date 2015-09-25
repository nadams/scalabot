package net.node3.scalabot

import scala.util.Try

object Implicits {
  implicit class RichOptionConvert(val s: String) extends AnyVal {
    def toIntOpt() = Try(s.toInt).toOption
  }
}
