package net.node3.scalabot

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
}
