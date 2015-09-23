package net.node3.scalabot.plugins

import scala.collection.immutable.Map

import akka.actor.ActorRef

import net.node3.scalabot.{ Plugin, PluginHelper, MessageSource }
import net.node3.scalabot.data._

class AccountPlugin extends Plugin with PluginHelper {
  import com.github.t3hnar.bcrypt._
  import org.joda.time.DateTime

  val rounds = 12
  val userRepository: UserRepository = new UserRepositoryImpl
  override val messages = Map[String, MessageHandler](
    "register" -> handleRegister,
    "identify" -> handleIdentify
  )

  def handleRegister(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    from.name.flatMap { ircName =>
      from.hostname.map { hostname =>
        message.split(" ") match {
          case Array(_, name, password, _*) =>
            if(userRepository.getUser(name).isEmpty) {
              userRepository.insertUser(name, password.bcrypt(rounds), s"$ircName@$hostname").map { u =>
                Seq(s"${u.name} was registered")
              } getOrElse(Seq(s"$name could not be registered"))
            } else Seq(s"$name could not be registered")
          case _ => Seq("Invalid format for the register command. The format is /msg botname register <name> <password>")
        }
      }
    }.getOrElse(Seq.empty)

  def handleIdentify(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    from.name.flatMap { ircName =>
      from.hostname.map { hostname =>
        message.split(" ") match {
          case Array(_, name, password, _*) =>
            userRepository.getUser(name).filter(u => password.isBcrypted(u.password)).map { user =>
              val combinedHost = s"$ircName@$hostname"
              userRepository.updateUser(user.userId, user.copy(hostname = combinedHost, lastIdentified = DateTime.now)).map { updatedUser =>
                Seq(s"Successfully identified as $name. The account's hostname has been updated to $combinedHost.")
              }.getOrElse(Seq("Could not update user"))
            }.getOrElse(Seq("Invalid username/password combination"))
          case _ => Seq("Invalid format for the identify command. The format is /msg botname identify <name> <password>")
        }
      }
    }.getOrElse(Seq.empty)
}


