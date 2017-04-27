package net.node3.scalabot.db

import java.nio.file._
import java.sql._

import net.node3.scalabot.config.Conf

trait DataCore {
  Class.forName("org.postgresql.Driver")

  implicit val connection: Connection = DriverManager.getConnection(Conf.dbURL)
}

