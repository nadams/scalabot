package net.node3.scalabot.plugins

import net.node3.scalabot.Plugin
import net.node3.scalabot.data._

class AccountPlugin extends Plugin {
  val userRepository: UserRepository = new UserRepositoryImpl

  def apply(from: String, to: String, message: String) : Option[String] = {
    if(message.toLowerCase().startsWith("register")) {
      message.split(" ") match {
        case Array(_, name, password, _*) =>
          if(userRepository.getUser(name).isEmpty) {
            userRepository.insertUser(name, password, from).map { u =>
              Some(s"${u.name} was registered")
            }.getOrElse(Some(s"$name could not be registered"))
          } else Some(s"$name could not be registered")
        case _ => Some("Invalid format for the register command. the format is /msg botname register <name> <password>")
      }
    } else {
      None
    }
  }

  def handlesMessage(from: String, to: String, message: String) =
    message.toLowerCase().startsWith("register")
}
