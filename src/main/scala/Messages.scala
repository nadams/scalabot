package net.node3.scalabot

import net.node3.scalabot.config._
import Tokens._

object Messages {
  object Ping {
    def apply(to: String) = Message(None, Command("PING"), List(to))
    def unapply(msg: Message) = msg match {
      case Message(_, Command("PING"), List(from)) => Some(from)
      case _ => None
    }
  }

  object Pong {
    def apply(to: String) = Message(None, Command("PONG"), List(to))
    def unapply(msg: Message) = msg match {
      case Message(_, Command("PONG"), List(from)) => Some(from)
      case _ => None
    }
  }

  object Nick {
    def apply(nick: String) = Message(None, Command("NICK"), List(nick))
    def unapply(msg: Message) = msg match {
      case Message(_, Command("NICK"), List(nick)) => Some(nick)
      case _ => None
    }
  }

  object User {
    def apply(username: String, hostname: String, domainname: String, realname: String) = Message(
      None,
      Command("USER"),
      List(
        username,
        hostname,
        domainname,
        realname
      )
    )

    def unapply(msg: Message) = msg match {
      case Message(_, Command("USER"), List(username, hostname, domainname, realname)) =>
        Some((username, hostname, domainname, realname))
      case _ => None
    }
  }

  object Pass {
    def apply(pass: String) = Message(None, Command("PASS"), List(pass))
    def unapply(msg: Message) = msg match {
      case Message(_, Command("PASS"), List(pass)) => Some(pass)
      case _ => None
    }
  }

  object Notice {
    def apply(to: String, message: String) = Message(None, Command("NOTICE"), List(to, message))
    def unapply(msg: Message) = msg match {
      case Message(Some(Prefix(from, _, _)), Command("NOTICE"), List(to, message)) =>
        Some((from, to, message))
      case _ => None
    }
  }

  object PrivMsg {
    def apply(to: String, message: String) = Message(None, Command("PRIVMSG"), List(to, message))
    def unapply(msg: Message) = msg match {
      case Message(Some(Prefix(from, _, _)), Command("PRIVMSG"), List(to, message)) =>
        Some((from, to, message))
      case _ => None
    }
  }

  object NotRegistered {
    def unapply(msg: Message) = msg match {
      case Message(Some(Prefix("NickServ", _, _)), Command("NOTICE"), List(to, message)) =>
        if(message.contains("Welcome to ")) Some((true))
        else None
      case _ => None
    }
  }

  object NickAlreadyRegistered {
    def unapply(msg: Message) = msg match {
      case Message(Some(Prefix("NickServ", _, _)), Command("NOTICE"), List(to, message)) =>
        if(message.contains("This nickname is registered.")) Some((true))
        else None
      case _ => None
    }
  }

  object IdentifyToNickServ {
    def apply(password: String) = PrivMsg("NickServ", s"IDENTIFY $password")
  }

  object RegisterToNickServ {
    def apply(email: String, password: String) = PrivMsg("NickServ", s"REGISTER $password $email")
  }

  object Login {
    def unapply(msg: Message) = msg match {
      case Message(Some(Prefix(from, _, _)), Command("PRIVMSG"), List(to, message)) =>
        if(message.contains("identify")) {
          val parts = message.split(" ")
          if(parts.length == 3) Some((parts(1), parts(2)))
          else None
        } else None
      case _ => None
    }
  }

  object JoinChannelCommand {
    def apply(channel: String) = Message(None, Command("JOIN"), List(channel))
    def unapply(msg: Message) = msg match {
      case Message(_, Command("PRIVMSG"), List(to, message)) =>
        if(message.contains("join")) {
          val parts = message.split(" ")
          if(parts.length == 2) {
            Some((parts(1)))
          } else None
        } else None
      case _ => None
    }
  }

  object BotCommand {
    def apply(to: String, message: String) = Message(None, Command("PRIVMSG"), List(to, message))
    def unapply(msg: Message)(implicit handler: BotCommandHandler) = msg match {
      case Message(Some(Prefix(from, _, _)), Command("PRIVMSG"), List(to, message)) =>
        if(handler.handlesMessage(from, to, message)) Some((from, to, message))
        else None
      case _ => None
    }
  }
}
