package net.node3.scalabot.db.migrate

object Migration {
  import scala.util.matching._
  import scala.io.Source
  import scala.language.postfixOps

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
case class MigrationRecord(migrationId: Int, filename: String, content: String, sha256: String)

object MigrationRecord {
  def apply(r :(Int, String, String, String)): MigrationRecord =
    MigrationRecord(r._1, r._2, r._3, r._4)
}

