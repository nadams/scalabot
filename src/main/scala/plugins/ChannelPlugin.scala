package net.node3.scalabot.plugins

import akka.actor.ActorRef

import net.node3.scalabot._
import net.node3.scalabot.data._

import Messages._

class ChannelPlugin extends Plugin with PluginHelper {
  val userRepository: UserRepository = new UserRepositoryImpl

  def handleJoin(from: MessageSource, to: String, message: String, bot: ActorRef): Option[String] =
    message.split(" ") match {
      case Array(_, channelName, _*) =>
        userRepository.getUser(from.source).map { user =>
          for(name <- from.name; hostname <- from.hostname) yield
            if(user.address == s"$name@$hostname") {
              bot ! JoinChannelCommand(channelName)
              ""
            } else {
              "Sorry, you are not authorized"
            }
        }.getOrElse(Some("Sorry, you are not authorized"))
      case _ => None
    }
}
