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
  def applyMigrations(migrationDirectory: String) : Unit = {
    if(!migrationTableExists()) {
      createMigrationTable()
    }
  }

  def migrationTableExists() : Boolean = SQL(
    """
      SELECT name
      FROM sqlite_master
      WHERE type='table'
        AND name='migrations'
    """
  ).as(scalar[String].singleOpt).isDefined

  def createMigrationTable() : Unit = SQL(
    """
      CREATE TABLE Migrations (
        MigrationId INT PRIMARY KEY,
        Filename TEXT NOT NULL UNIQUE,
        DateApplied TEXT NOT NULL,
        Content TEXT NOT NULL
      )
    """
  ).execute
}
