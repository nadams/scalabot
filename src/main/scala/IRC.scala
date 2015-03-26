package net.node3.scalabot

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }
import akka.util.{ ByteString, ByteStringBuilder }
import java.net.InetSocketAddress

object IRC {
  def props(remote: InetSocketAddress, replies: ActorRef) =
    Props(classOf[IRC], remote, replies)
}

sealed trait IrcEvent
case class IRCConnect(remote: InetSocketAddress)
case object Connected extends IrcEvent
case object Stop extends IrcEvent
case object Stopped extends IrcEvent

class IRC(remote: InetSocketAddress, listener: ActorRef) extends Actor {
  import Tcp._
  import Tokens._
  import context.system

  var handler: ActorRef = _

  override def preStart() = {
    self ! IRCConnect(remote)
  }

  override def postStop() = {
    context.children.foreach(context.stop)
  }

  def receive = {
    case IRCConnect(remote) =>
      IO(Tcp) ! Tcp.Connect(remote)
    case CommandFailed(_: Connect) =>
      listener ! "connect failed"
      context.stop(self)
    case c @ Tcp.Connected(remote, local) =>
      val connection = sender()
      handler = context.actorOf(Handler.props(remote, connection, listener, "UTF-8", "shutting down"))
      connection ! Tcp.Register(handler)
      context.watch(handler)
      context.become {
        case CommandFailed(w: Write) =>
          listener ! "write failed"
        case _: ConnectionClosed =>
          listener ! "connection closed"
          context.stop(self)
        case Stop =>
          if(context.children.isEmpty) sender() ! Stopped
          handler.forward(Stop)
        case response: Response =>
          println("forwarding")
          handler.forward(response)
        case Stopped =>
          sender ! Stopped
      }
    case Stop =>
      sender ! Stopped
  }
}
