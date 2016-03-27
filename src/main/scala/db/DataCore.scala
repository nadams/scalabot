package net.node3.scalabot.db

import java.sql._

import net.node3.scalabot.config.Conf

trait DataCore {
  Class.forName("org.sqlite.JDBC")

  val host = s"jdbc:sqlite:${Conf.dbFile}"

  implicit val connection: Connection = DriverManager.getConnection(host)
  connection.nativeSQL("PRAGMA foreign_keys = ON;")
}

