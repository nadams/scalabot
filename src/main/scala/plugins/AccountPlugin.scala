package net.node3.scalabot.plugins

import scala.collection.immutable.Map

import net.node3.scalabot.{ Plugin, MessageSource }
import net.node3.scalabot.data._

class AccountPlugin extends Plugin {
  import com.github.t3hnar.bcrypt._
  import org.joda.time.DateTime

  val rounds = 12
  val userRepository: UserRepository = new UserRepositoryImpl
  val messages = Map[String, (MessageSource, String, String) => Option[String]](
    "register" -> handleRegister,
    "identify" -> handleIdentify
  )

  def apply(from: MessageSource, to: String, message: String): Option[String] =
    message.split(" ") match {
      case Array(command, _*) => messages.get(command).map(_(from, to, message)).getOrElse(None)
      case _ => None
    }

  def handlesMessage(from: MessageSource, to: String, message: String) =
    messages.keySet.exists(message.toLowerCase.startsWith(_))

  def handleRegister(from: MessageSource, to: String, message: String): Option[String] =
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

  def handleIdentify(from: MessageSource, to: String, message: String): Option[String] =
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
}


object PermissionFlags {
  type Permissions = Int

  val User = 1 << 0
  val Admin = 1 << 1
  val Owner = 1 << 2

  private val is: (Permissions, Permissions) => Boolean = (x, y) => (x & y) == x

  def isUser(flags: Permissions) = (flags & User) == User
  def isAdmin(flags: Permissions) = (flags & Admin) == Admin
  def isOwner(flags: Permissions) = (flags & Owner) == Owner
  def isAll(flag: Permissions, flags: Seq[Permissions]) = flags.forall(f => is(f, flag))
  def isAny(flag: Permissions, flags: Seq[Permissions]) = flags.exists(f => is(f, flag))
}

