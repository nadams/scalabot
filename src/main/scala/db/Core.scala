package net.node3.scalabot.db

import java.sql._

import anorm._
import anorm.SqlParser._

trait DataCore {
  Class.forName("org.sqlite.JDBC");

  val host = "jdbc:sqlite:db/db.sqlite"

  implicit val connection: Connection = DriverManager.getConnection(host)
}

object Migrations extends DataCore {
  import java.io._
  import scala.collection.JavaConversions._
  import scala.io.Source

  import com.github.nscala_time.time.Imports._
  import com.roundeights.hasher.Implicits._

  def applyMigrations(migrationDirectory: String) : Unit = {
    val migrations = new File("db/migrations")

    if(!migrationTableExists()) {
      createMigrationTable()
      runAllMigrations(migrations)
    } else {
      runAllMigrations(migrations)
    }
  }

  def migrationTableExists() : Boolean = SQL(
    """
      SELECT name
      FROM sqlite_master
      WHERE type = 'table'
        AND name = 'Migrations'
    """
  ).as(scalar[String].singleOpt).isDefined

  def createMigrationTable() : Unit = SQL(
    """
      CREATE TABLE Migrations (
        MigrationId INTEGER PRIMARY KEY AUTOINCREMENT,
        Filename TEXT NOT NULL UNIQUE,
        DateApplied TEXT NOT NULL,
        SHA256 TEXT NOT NULL,
        Content TEXT NOT NULL
      )
    """
  ).execute

  def runAllMigrations(dir: File) : Unit =
    dir.listFiles.filter(_.getName.endsWith(".sql")).foreach { migration =>
      val m = Migration(migration.getPath)
      try {
        SQL(m.up).execute
        val fileContent = Source.fromFile(migration).mkString
        val fileSum = fileContent.sha256.hex
        SQL"""
          INSERT INTO Migrations
          VALUES (
            NULL,
            ${migration.getName},
            ${DateTime.now.toString},
            $fileContent,
            $fileSum
          )
        """.execute
      } catch {
        case e: Throwable  => println(e)
      }
    }
}
