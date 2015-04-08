package net.node3.scalabot.plugins

import net.node3.scalabot.{ Plugin, MessageSource }
import net.node3.scalabot.data._

class AccountPlugin extends Plugin {
  import com.github.t3hnar.bcrypt._
  import org.joda.time.DateTime

  val rounds = 12
  val userRepository: UserRepository = new UserRepositoryImpl

  def apply(from: MessageSource, to: String, message: String) : Option[String] =
    if(message.toLowerCase.startsWith("register")) {
      for(ircName <- from.name; hostname <- from.hostname) yield
        message.split(" ") match {
          case Array(_, name, password, _*) =>
            if(userRepository.getUser(name).isEmpty) {
              userRepository.insertUser(name, password.bcrypt(rounds), s"$ircName@$hostname").map { u =>
                s"${u.name} was registered"
              } getOrElse(s"$name could not be registered")
            } else s"$name could not be registered"
          case _ => "Invalid format for the register command. The format is /msg botname register <name> <password>"
        }
    } else if(message.toLowerCase.startsWith("identify")) {
      for(ircName <- from.name; hostname <- from.hostname) yield
        message.split(" ") match {
          case Array(_, name, password, _*) =>
            userRepository.getUser(name).filter(u => password.isBcrypted(u.password)).map { user =>
              val combinedHost = s"$ircName@$hostname"
              userRepository.updateUser(user.userId, user.copy(hostname = combinedHost, lastIdentified = DateTime.now)).map { updatedUser =>
                s"Successfully identified as $name. The account's hostname has been updated to $combinedHost."
              }.getOrElse("Could not update user")
            }.getOrElse("Invalid username/password combination")
          case _ => "Invalid format for the identify command. The format is /msg botname identify <name> <password>"
        }

    } else {
      None
    }

  def handlesMessage(from: MessageSource, to: String, message: String) =
    message.toLowerCase.startsWith("register") || message.toLowerCase.startsWith("identify")
}
