package net.node3.scalabot.plugins

import akka.actor.ActorRef

import net.node3.scalabot._
import net.node3.scalabot.data._
import net.node3.scalabot.config.Conf

import Tokens.{ Command, Message }

class RawCommandPlugin extends Plugin with PluginHelper {
  val botName = Conf.nick
  val userRepository: UserRepository = new UserRepositoryImpl
  override val messages = Map[String, MessageHandler](
    "raw" -> handleRaw
  )

  def handleRaw(from: MessageSource, to: String, message: String, bot: ActorRef): Option[String] =
    message.split(" ", 2) match {
      case Array(_, command) =>
        if(to == botName) {
          userRepository.getUser(from.source).map { user =>
            for(name <- from.name; hostname <- from.hostname) yield
              if(user.hostname == s"$name@$hostname" && PermissionFlags.isOwner(user.permissions)) {
                bot ! Message(None, Command(command), List.empty[String])
                ""
              } else ""
          }.getOrElse(None)
        } else None
      case _ => None
    }
}
