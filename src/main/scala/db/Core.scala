package net.node3.scalabot.db

import java.sql._

trait DataCore {
  Class.forName("org.sqlite.JDBC");

  val host = "jdbc:sqlite:db/db.sqlite"

  implicit val connection: Connection = DriverManager.getConnection(host)
}

