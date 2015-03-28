package net.node3.scalabot.db

import anorm._

trait UserRepository {
  def getUser(name: String) : Option[String]
}

class UserRepositoryImpl extends UserRepository with DataCore {
  def getUser(name: String) = {
    SQL"SELECT * FROM User"
    Some(s"hello $name")
  }
}
