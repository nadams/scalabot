package net.node3.scalabot.plugins

import scala.collection.immutable.Map
import scala.collection.mutable.{ Map => MutableMap }
import scala.util.Random

import akka.actor.ActorRef
import com.typesafe.config.Config

import net.node3.scalabot.Implicits._
import net.node3.scalabot.Messages
import net.node3.scalabot.config.Conf
import net.node3.scalabot.{ Plugin, PluginHelper, MessageSource }
import net.node3.scalabot.data._
import net.node3.scalabot.plugins.cards._

class CardsPlugin extends Plugin with PluginHelper {
  type ChannelAction = (String, String, String, ActorRef) => Seq[String]
  type GameAction = (Game, String, String, String, ActorRef) => Seq[String]

  override val messages = Map[String, MessageHandler](
    "cards" -> handleCards
  ) ++ Seq.range(0, 10).map(x => (x + 1).toString -> handleCards _)

  private val channelRegex = """(#.+)""".r
  private val games = MutableMap[String, Game]()
  private val config = CardsConfig(Conf.config.getConfig("bot.cards"))
  private val cards = Cards(config.cardsPath, config.numBlanks)

  override def handlesMessage(from: MessageSource, to: String, message: String): Boolean =
    super.handlesMessage(from, to, message) || games.get(from.source).isDefined

  def handleCards(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] = synchronized {
    message.toLowerCase.split(" ") match {
      case Array(_, "start") => channelAction(from, to, message, bot)(startCards)
      case Array(_, "join") => gameAction(from, to, message, bot)(joinGame)
      case Array(_, "go") => gameAction(from, to, message, bot)(startGame)
      case Array(_, "stop") => gameAction(from, to, message, bot)(stopGame)
      case Array(_, number) => getChannel(from).map(selectCards(from, to, message, bot, _, number)).getOrElse(Seq.empty)
      case Array(number) => selectCards(from, to, message, bot, from.source, number)
      case Array(_, channel, "select", number) => selectCards(from, to, message, bot, channel, number)
      case _ => Seq.empty
    }
  }

  def selectCards(from: MessageSource, to: String, message: String, bot: ActorRef, chan: String, number: String): Seq[String] =
    getChannel(chan).orElse {
      val gamesWithPlayer: MutableMap[String, Game] = games.filter { case(channelString, game) =>
        game.players.contains(from.source)
      }

      if(gamesWithPlayer.size > 1) None
      else gamesWithPlayer.keySet.headOption
    }.map { channel =>
      games.get(channel).map { game =>
        if(game.czar == to && game.cardPickers.get(from.source).isEmpty && game.allPlayersHavePlayed) {
          number.toIntOpt.foreach { cardNumber =>
            game.pickAnswer(cardNumber, cards) match {
              case (winner, game) =>
                games.update(channel, game)
                bot ! Messages.PrivMsg(channel, s"${winner.name} wins")
                game.stepGame(channel, bot)
            }
          }
        } else {
          game.cardPickers.get(from.source).map(game.pickCard(_, message, bot)).map { game =>
            games.update(channel, game)
            if(game.allPlayersHavePlayed) {
              val updatedGame = game.selectAnswers()
              games.update(channel, updatedGame)

              bot ! Messages.PrivMsg(channel, s"${updatedGame.czar} pick a card")
              println(updatedGame)
              updatedGame.playerAnswers.keys.toSeq.sortBy(x => x).foreach { key =>
                bot ! Messages.PrivMsg(channel, s"($key) ${updatedGame.playerAnswers(key).answerString}")
              }
            }
          }
        }
      }
      Seq.empty
    }.getOrElse(Seq("Could not determine channel you're playing, select with the command `cards <channel> select <number>`"))

  def startGame(game: Game, channel: String, to: String, message: String, bot: ActorRef): Seq[String] =
    if(game.players.size < 2) {
      Seq("Must have more than 1 player to start the game.")
    } else {
      val updatedGame = game.copy(
        czar = game.players.keys.head,
        question = game.nextBlackCard(cards.blackCards)
      )

      games.update(channel, updatedGame)
      updatedGame.stepGame(channel, bot)

      Seq.empty
    }

  def stopGame(game: Game, channel: String, to: String, message: String, bot: ActorRef): Seq[String] = {
    games.remove(channel)
    game.sendScores(channel, bot)
    Seq.empty
  }

  def joinGame(game: Game, channel: String, to: String, message: String, bot: ActorRef): Seq[String] =
    if(!game.players.contains(to)) {
      games.update(channel, game.copy(players = game.players + (to -> Player(to, Player.takeCards(cards.whiteCards)))))
      Seq(s"$to has joined the game", "Type `cards go` to start the game.")
    } else Seq.empty

  def startCards(channel: String, to: String, message: String, bot: ActorRef): Seq[String] =
    if(games.contains(channel)) {
      s"A game is already in progress for channel: $channel."
    } else {
      games.put(channel, Game())
      Seq("Type `cards join` in this channel to join the game.", "Type `cards start` to start the game.")
    }

  private def gameAction(from: MessageSource, to: String, message: String, bot: ActorRef)(action: GameAction): Seq[String] =
    getChannel(from).map { channel =>
      games.get(channel).map(action(_, channel, to, message, bot)).getOrElse(Seq("There isn't a game currently running."))
    }.getOrElse("This message must be performed on a channel.")

  private def channelAction(from: MessageSource, to: String, message: String, bot: ActorRef)(action: ChannelAction): Seq[String] =
    getChannel(from).map(action(_, to, message, bot)).getOrElse("This message must be performed on a channel.")

  private def getChannel(from: MessageSource): Option[String] = getChannel(from.source)

  private def getChannel(chan: String): Option[String] = chan match {
    case channelRegex(channel) => Some(channel)
    case _ => None
  }
}
