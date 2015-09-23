package net.node3.scalabot.plugins

import scala.collection.immutable.Map
import scala.collection.mutable.{ Map => MutableMap }

import akka.actor.ActorRef

import net.node3.scalabot.{ Plugin, PluginHelper, MessageSource }
import net.node3.scalabot.data._

case class Player(val points: Int, val cards: Seq[String])

object Player {
  def apply(): Player = Player(0)
}

class Game(
  val players: MutableMap[String, Player],
  var czar: String
  var state: Boolean
)

class CardsPlugin extends Plugin with PluginHelper {
  val channelRegex = """(#.+)""".r

  override val messages = Map[String, MessageHandler]("cards" -> cards)

  private val games = MutableMap[String, Game]()

  def cards(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    message.toLowerCase.split(" ") match {
      case Array(_, "start", _*) => startCards(from, to, message, bot)
      case Array(_, "join", _*) => joinGame(from, to, message, bot)
      case Array(_, "go", _*) => ???
      case Array(_, channel, "select", number, _*) => ???
      case _ => Seq.empty
    }

  def joinGame(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    getChannel(from).map { channel =>
      games.get(channel).map { game =>
        if(!game.players.contains(to)) {
          game.players += to -> Player()
          Seq(s"$to has joined the game", "Type `cards go` to start the game.")
        } else Seq.empty
      }.getOrElse(Seq("There isn't a game currently running."))
    }.getOrElse(Seq.empty)

  def startCards(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    getChannel(from).map { channel =>
      games.get(channel).map { existingChannel =>
        Seq(s"A game is already in progress for channel: $existingChannel.")
      }.getOrElse {
        games.put(channel, new Game(MutableMap.empty, ""))
        Seq("Type `cards join` in this channel to join the game.", "Type `cards start` to start the game.")
      }
    }.getOrElse(Seq("This command must be performed in a channel."))

  private def getChannel(from: MessageSource): Option[String] = from.source match {
    case channelRegex(channel) => Some(channel)
    case _ => None
  }
}
