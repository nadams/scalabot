package net.node3.scalabot.plugins

import net.node3.scalabot.Plugin
import net.node3.scalabot.data._

class AccountPlugin extends Plugin {
  val userRepository: UserRepository = new UserRepositoryImpl

  def apply(from: String, to: String, message: String) : Option[String] = {
    if(message.toLowerCase().startsWith("register")) {
      message.split(" ") match {
        case Array(_, name, password, _*) =>
          userRepository.insertUser(name, password).map { u =>
            Some(s"${u.name} was registered")
          }.getOrElse(None)
        case _ => None
      }
    } else {
      None
    }
  }

  def handlesMessage(from: String, to: String, message: String) =
    message.toLowerCase().startsWith("register")
}
