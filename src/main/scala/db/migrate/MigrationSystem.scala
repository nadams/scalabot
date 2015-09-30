package net.node3.scalabot.db.migrate

import scala.language.postfixOps

import anorm._
import anorm.SqlParser._

import net.node3.scalabot.db.DataCore

object MigrationSystem extends DataCore {
  import java.io._
  import scala.collection.JavaConversions._
  import scala.io.Source
  import com.roundeights.hasher.Implicits._

  def applyMigrations(migrationDirectory: String): Unit = {
    val migrations = new File(migrationDirectory)

    if(!migrationTableExists()) {
      createMigrationTable()
      runAllMigrations(migrations)
    } else {
      updateMigrations(migrations)
    }
  }

  def updateMigrations(dir: File): Unit = {
    val records = SQL"""
      SELECT
        MigrationId,
        Filename,
        SHA256,
        Content
      FROM Migrations
      ORDER BY Filename DESC
    """.as(int("MigrationId") ~ str("Filename") ~ str("SHA256") ~ str("Content") map(flatten) *)
    .map(MigrationRecord(_)).map { x => x.filename -> x } toMap

    val files = filterToSql(dir).map { x => x.getName -> x } toMap
    val unappliedMigrations = files.keySet.diff(records.keySet)

    unappliedMigrations.toSeq.sortBy(x => x).foreach(x => applyMigration(files(x)))
  }

  def migrationTableExists(): Boolean = SQL(
    """
      SELECT name
      FROM sqlite_master
      WHERE type = 'table'
        AND name = 'Migrations'
    """
  ).as(scalar[String].singleOpt).isDefined

  def createMigrationTable(): Unit = SQL(
    """
      CREATE TABLE Migrations (
        MigrationId INTEGER PRIMARY KEY AUTOINCREMENT,
        Filename TEXT NOT NULL UNIQUE,
        SHA256 TEXT NOT NULL,
        Content TEXT NOT NULL
      )
    """
  ).execute

  def runAllMigrations(dir: File): Unit = filterToSql(dir).foreach(applyMigration(_))

  def applyMigration(migration: File): Unit = {
    val m = Migration(migration.getPath)
    m.up.split("(?<!;);(?!;)").map(_.trim.replace(";;", ";")).filter(_ != "").foreach(SQL(_).execute())

    val fileContent = Source.fromFile(migration).mkString
    val fileSum = fileContent.sha256.hex
    SQL"""
      INSERT INTO Migrations
      VALUES (
        NULL,
        ${migration.getName},
        $fileContent,
        $fileSum
      )
    """.execute
  }

  def filterToSql(dir: File): Seq[File] = dir.listFiles.filter(_.getName.endsWith(".sql")).toSeq
}
