package net.node3.scalabot.db

import java.sql._

import net.node3.scalabot.config.Conf

trait DataCore {
  Class.forName("org.sqlite.JDBC")

  val dbFile = Conf.dbFile
  val host = s"jdbc:sqlite:$dbFile"

  implicit val connection: Connection = DriverManager.getConnection(host)
}

