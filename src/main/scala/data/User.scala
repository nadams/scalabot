package net.node3.scalabot.data

import scala.language.postfixOps

import java.util.Date

import com.github.nscala_time.time.Imports._
import anorm._
import anorm.SqlParser._

import net.node3.scalabot.db.AnormExtensions._
import net.node3.scalabot.db.DataCore

trait UserRepository {
  def hasUsers() : Boolean
  def getUser(name: String) : Option[User]
  def getUserById(id: Int) : Option[User]
  def insertUser(name: String, password: String, hostname: String): Option[User]
  def updateUser(id: Int, user: User): Option[User]
}

class UserRepositoryImpl extends UserRepository with DataCore {
  def updateUser(id: Int, user: User): Option[User] =
    if(SQL"""
      UPDATE user
      SET
        name = ${user.name},
        password = ${user.password},
        date_created = ${user.dateCreated},
        hostname = ${user.hostname},
        permissions = ${user.permissions},
        last_identified = ${user.lastIdentified}
      WHERE id = $id
      """.executeUpdate > 0) Some(user)
    else None

  def insertUser(name: String, password: String, hostname: String): Option[User] = {
    val id = SQL"""
      INSERT INTO user(name, password, date_created, hostname, permissions, last_identified)
      VALUES ($name, $password, ${DateTime.now}, $hostname, 0, ${new DateTime(new Date(0), DateTimeZone.UTC)})
    """.executeInsert(scalar[Int] single)

    if(id > 0) {
      getUserById(id)
    } else None
  }

  def getUserById(id: Int): Option[User] =
    SQL"""
      SELECT
        id AS id,
        name AS name,
        password AS password,
        date_created AS date_created,
        hostname,
        permissions,
        last_identified AS last_identified
      FROM user
      WHERE id = $id
    """.as(User.singleRowParser singleOpt).map(User(_))

  def getUser(name: String): Option[User] =
    SQL"""
      SELECT
        id AS id,
        name AS name,
        password AS password,
        date_created AS date_created,
        hostname,
        permissions,
        last_identified AS last_identified
      FROM user
      WHERE name = $name
    """.as(User.singleRowParser singleOpt).map(User(_))

  def hasUsers(): Boolean =
    SQL"""
      SELECT COUNT(*) FROM user
    """.as(scalar[Int].single) > 0
}

case class User(userId: Int, name: String, password: String, dateCreated: DateTime, hostname: String, permissions: Permissions, lastIdentified: DateTime)

object User {
  lazy val singleRowParser =
    int("id") ~ str("name") ~ str("password") ~ datetime("date_created") ~ str("hostname") ~ int("permissions") ~ datetime("last_identified") map(flatten)

  lazy val multiRowParser = singleRowParser *

  def apply(x: (Int, String, String, DateTime, String, Permissions, DateTime)): User = User(x._1, x._2, x._3, x._4, x._5, x._6, x._7)
}
