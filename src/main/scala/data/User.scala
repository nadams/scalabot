package net.node3.scalabot.data

import com.github.nscala_time.time.Imports._
import anorm._
import anorm.SqlParser._

import net.node3.scalabot.db.AnormExtensions._
import net.node3.scalabot.db.DataCore

trait UserRepository {
  def hasUsers() : Boolean
  def getUser(name: String) : Option[User]
  def getUserById(id: Int) : Option[User]
  def insertUser(name: String, password: String): Option[User]
}

class UserRepositoryImpl extends UserRepository with DataCore {
  def insertUser(name: String, password: String): Option[User] = {
    val id = SQL"""
      INSERT INTO User(Name, Password, DateCreated)
      VALUES ($name, $password, ${DateTime.now})
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
        DateCreated
      FROM User
      WHERE UserId = $id
    """.as(User.singleRowParser singleOpt).map(User(_))

  def getUser(name: String): Option[User] =
    SQL"""
      SELECT
        UserId,
        Name,
        Password,
        DateCreated
      FROM User
      WHERE name = $name
    """.as(User.singleRowParser singleOpt).map(User(_))

  def hasUsers(): Boolean =
    SQL"""
      SELECT COUNT(*) FROM User
    """.as(scalar[Int].single) > 0
}

case class User(userId: Int, name: String, password: String, dateCreated: DateTime)

object User {
  lazy val singleRowParser = int("UserId") ~ str("Name") ~ str("Password") ~ datetime("DateCreated") map(flatten)
  lazy val multiRowParser = singleRowParser *

  def apply(x: (Int, String, String, DateTime)): User = User(x._1, x._2, x._3, x._4)
}
