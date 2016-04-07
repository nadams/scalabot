package net.node3.scalabot.db.migrate

import scala.language.postfixOps
import java.security.MessageDigest

import anorm._
import anorm.SqlParser._

import net.node3.scalabot.db.DataCore
import net.node3.scalabot.config.Conf

object MigrationSystem extends DataCore {
  import scala.collection.JavaConversions._
  import scala.io.Source

  import java.io._
  import java.nio.file.Paths

  def applyMigrations(): Unit = {
    val migrationPath = Conf.dbDir + "/migrations"
    val migrations = filterToSql(new File(migrationPath).list().map(migrationPath + "/" + _)).sortBy(x => x)

    if(!migrationTableExists()) {
      createMigrationTable()
      runAllMigrations(migrations)
    } else {
      updateMigrations(migrations)
    }
  }

  def updateMigrations(files: Seq[String]): Unit = {
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

    val filteredFiles = filterToSql(files).map(x => getFilename(x) -> x).toMap
    val unappliedMigrations = filteredFiles.keySet.diff(records.keySet)

    unappliedMigrations.toSeq.sortBy(x => x).foreach(x => applyMigration(filteredFiles(x)))
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

  def runAllMigrations(files: Seq[String]): Unit = filterToSql(files).foreach(applyMigration(_))

  def applyMigration(file: String): Unit = {
    val m = Migration(file)
    m.up.split("(?<!;);(?!;)").map(_.trim.replace(";;", ";")).filter(_ != "").foreach(SQL(_).execute())

    val fileContent = Source.fromFile(file).mkString
    val fileSum = sha256(fileContent)
    val filename = getFilename(file)

    SQL"""
      INSERT INTO Migrations
      VALUES (
        NULL,
        ${filename},
        $fileContent,
        $fileSum
      )
    """.execute
  }

  def filterToSql(files: Seq[String]): Seq[String] = files.filter(_.endsWith(".sql")).toSeq

  def getFilename(file: String): String = Paths.get(file).getFileName.toString

  private def sha256(content: String): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update(content.getBytes("UTF-8"))
    String.format("%064x", new java.math.BigInteger(1, digest.digest()))
  }
}
