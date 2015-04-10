package net.node3.scalabot.plugins

import net.node3.scalabot.{ Plugin, PluginHelper, MessageSource }
import net.node3.scalabot.data._

class ChannelPlugin extends Plugin with PluginHelper {
  val userRepository: UserRepository = new UserRepositoryImpl

  def handleJoin(from: MessageSource, to: String, message: String): Option[String] =
    message.split(" ") match {
      case Array(_, channelName, _*) =>
        userRepository.getUser(from.source).map { user =>
          for(name <- from.name; hostname <- from.hostname) yield
            if(user.address == s"$name@$hostname") {
              //Akka.system.actorSelector(""
              ""
            } else {
              "Sorry, you are not authorized"
            }
        }.getOrElse(Some("Sorry, you are not authorized"))
      case _ => None
    }
}
