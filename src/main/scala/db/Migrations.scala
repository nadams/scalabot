package net.node3.scalabot.db

object Migration {
  import scala.util.matching._
  import scala.io.Source

  val sectionRegex = """#\s*---\s*!""".r

  def apply() : Migration = Migration("", "")

  def apply(filepath: String) : Migration = {
    val lines = Source.fromFile(filepath).mkString
    val sections = sectionRegex.split(lines)
    sections.flatMap { section =>
      val sectionTypePos = section.indexOf('\n')
      if(sectionTypePos > -1) {
        val sectionType = section.substring(0, sectionTypePos)
        val sectionContent = section.substring(sectionTypePos)
        Some((sectionType, sectionContent))
      } else None
    }.foldLeft(Migration()) { (acc, value) =>
      if(value._1 == "Ups") acc.copy(up = value._2)
      else if(value._1 == "Downs") acc.copy(down = value._2)
      else acc
    }
  }
}

case class Migration(up: String, down: String)
