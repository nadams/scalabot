package net.node3.scalabot.plugins

import scala.collection.immutable.Seq

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

  def handleAdd(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    channelAction(from, to, message, bot) { channelName => Seq.empty }

  def handleJoin(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    channelAction(from, to, message, bot) { channelName =>
      bot ! JoinChannelCommand(channelName)
      Seq.empty
    }

  def channelAction(from: MessageSource, to: String, message: String, bot: ActorRef)(action: String => Seq[String]): Seq[String] =
    message.split(" ") match {
      case Array(_, channelName, _*) =>
        if(to == botName) {
          userRepository.getUser(from.source).map { user =>
            from.name.flatMap { name =>
              from.hostname.map { hostname =>
                if(user.hostname == s"$name@$hostname" && PermissionFlags.isAdmin(user.permissions)) {
                  action(channelName)
                } else Seq.empty
              }
            }.getOrElse(Seq.empty)
          }.getOrElse(Seq.empty)
        } else Seq.empty
      case _ => Seq.empty
    }
}
