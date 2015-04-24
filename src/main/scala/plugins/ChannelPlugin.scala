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
    "join" -> handleJoin
  )

  def handleJoin(from: MessageSource, to: String, message: String, bot: ActorRef): Option[String] =
    message.split(" ") match {
      case Array(_, channelName, _*) =>
        if(to == botName) {
          userRepository.getUser(from.source).map { user =>
            for(name <- from.name; hostname <- from.hostname) yield
              if(user.hostname == s"$name@$hostname" && PermissionFlags.isAdmin(user.permissions)) {
                bot ! JoinChannelCommand(channelName)
                ""
              } else ""
          }.getOrElse(Some(""))
        } else None
      case _ => None
    }
}
