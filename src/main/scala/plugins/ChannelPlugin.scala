package net.node3.scalabot.plugins

import akka.actor.ActorRef

import net.node3.scalabot._
import net.node3.scalabot.data._
import net.node3.scalabot.config.Conf

import Messages._

class ChannelPlugin extends Plugin with PluginHelper {
  val botName = Conf.nick
  val userRepository: UserRepository = new UserRepositoryImpl
  override val messages = Map[String, MessageHandler](
    "join" -> handleJoin,
    "add" -> handleAdd
  )

  def handleAdd(from: MessageSource, to: String, message: String, bot: ActorRef): Option[String] =
    channelAction(from, to, message, bot) { channelName =>
      ""
    }

  def handleJoin(from: MessageSource, to: String, message: String, bot: ActorRef): Option[String] =
    channelAction(from, to, message, bot) { channelName =>
      bot ! JoinChannelCommand(channelName)
      ""
    }

  def channelAction(from: MessageSource, to: String, message: String, bot: ActorRef)(action: String => String): Option[String] =
    message.split(" ") match {
      case Array(_, channelName, _*) =>
        if(to == botName) {
          userRepository.getUser(from.source).map { user =>
            for(name <- from.name; hostname <- from.hostname) yield
              if(user.hostname == s"$name@$hostname" && PermissionFlags.isAdmin(user.permissions)) {
                action(channelName)
              } else ""
          }.getOrElse(Some(""))
        } else None
      case _ => None
    }
}
