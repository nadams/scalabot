package net.node3.scalabot.plugins

import scala.collection.immutable.Map
import scala.collection.mutable.{ Map => MutableMap }

import akka.actor.ActorRef
import com.typesafe.config.Config

import net.node3.scalabot.config.Conf
import net.node3.scalabot.{ Plugin, PluginHelper, MessageSource }
import net.node3.scalabot.data._
import net.node3.scalabot.plugins.cards._

class CardsPlugin extends Plugin with PluginHelper {
  type ChannelAction = (String, String, String, ActorRef) => Seq[String]

  override val messages = Map[String, MessageHandler]("cards" -> cards)
  private val channelRegex = """((#|&).+)""".r
  private val games = MutableMap[String, Game]()
  private val config = CardsConfig(Conf.config.getConfig("bot.cards").getString("cardsPath"))
  private val cards = Cards(config.cardsPath)

  implicit def stringToSeq(s: String): Seq[String] =
    if(s.length == 0) Seq.empty
    else Seq(s)

  def cards(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    message.toLowerCase.split(" ") match {
      case Array(_, "start", _*) => channelAction(from, to, message, bot)(startCards)
      case Array(_, "join", _*) => channelAction(from, to, message, bot)(joinGame)
      case Array(_, "go", _*) => channelAction(from, to, message, bot)(startGame)
      case Array(_, channel, "select", number, _*) => Seq(channel, "select", number)
      case _ => Seq.empty
    }

  def startGame(channel: String, to: String, message: String, bot: ActorRef): Seq[String] =
    ???
    //games.get(channel).map { game =>
    //  if(game.players.size < 2) Seq("Not enough players.")
    //  else ???
    //  // check player count
    //  // set game state to in progress
    //  // pick czar
    //  // message all players their questions
    //}.getOrElse(Seq("There isn't a game currently running."))

  def stopGame(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    ???

  def joinGame(channel: String, to: String, message: String, bot: ActorRef): Seq[String] =
    games.get(channel).map { game =>
      if(game.state == GameStates.Init && !game.players.contains(to)) {
        game.players += to -> Player()
        Seq(s"$to has joined the game", "Type `cards go` to start the game.")
      } else Seq.empty
    }.getOrElse("There isn't a game currently running.")

  def startCards(channel: String, to: String, message: String, bot: ActorRef): Seq[String] =
    if(games.contains(channel)) {
      s"A game is already in progress for channel: $channel."
    } else {
      games.put(channel, Game())
      Seq("Type `cards join` in this channel to join the game.", "Type `cards start` to start the game.")
    }

  private def channelAction(from: MessageSource, to: String, message: String, bot: ActorRef)(action: ChannelAction): Seq[String] =
    getChannel(from).map(action(_, to, message, bot)).getOrElse("This message must be performed on a channel.")

  private def getChannel(from: MessageSource): Option[String] = from.source match {
    case channelRegex(channel) => Some(channel)
    case _ => None
  }
}
