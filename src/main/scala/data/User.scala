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
      UPDATE User
      SET
        Name = ${user.name},
        Password = ${user.password},
        DateCreated = ${user.dateCreated},
        Hostname = ${user.hostname},
        Permissions = ${user.permissions},
        LastIdentified = ${user.lastIdentified}
      WHERE UserId = $id
      """.executeUpdate > 0) Some(user)
    else None

  def insertUser(name: String, password: String, hostname: String): Option[User] = {
    val id = SQL"""
      INSERT INTO User(Name, Password, DateCreated, Hostname, Permissions, LastIdentified)
      VALUES ($name, $password, ${DateTime.now}, $hostname, 0, ${new DateTime(new Date(0), DateTimeZone.UTC)})
    """.executeInsert(scalar[Int] single)

    if(id > 0) {
      getUserById(id)
    } else None
  }

  def getUserById(id: Int): Option[User] =
    SQL"""
      SELECT
        UserId,
        Name,
        Password,
        DateCreated,
        Hostname,
        Permissions,
        LastIdentified
      FROM User
      WHERE UserId = $id
    """.as(User.singleRowParser singleOpt).map(User(_))

  def getUser(name: String): Option[User] =
    SQL"""
      SELECT
        UserId,
        Name,
        Password,
        DateCreated,
        Hostname,
        Permissions,
        LastIdentified
      FROM User
      WHERE Name = $name
    """.as(User.singleRowParser singleOpt).map(User(_))

  def hasUsers(): Boolean =
    SQL"""
      SELECT COUNT(*) FROM User
    """.as(scalar[Int].single) > 0
}

case class User(userId: Int, name: String, password: String, dateCreated: DateTime, hostname: String, permissions: Permissions, lastIdentified: DateTime)

object User {
  lazy val singleRowParser =
    int("UserId") ~ str("Name") ~ str("Password") ~ datetime("DateCreated") ~ str("Hostname") ~ int("Permissions") ~ datetime("LastIdentified") map(flatten)

  lazy val multiRowParser = singleRowParser *

  def apply(x: (Int, String, String, DateTime, String, Permissions, DateTime)): User = User(x._1, x._2, x._3, x._4, x._5, x._6, x._7)
}
