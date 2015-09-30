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

  override val messages = Map[String, MessageHandler]("cards" -> synchronized { cards })
  private val channelRegex = """(#.+)""".r
  private val games = MutableMap[String, Game]()
  private val config = CardsConfig(Conf.config.getConfig("bot.cards").getString("cardsPath"))
  private val cards = Cards(config.cardsPath)

  def cards(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    message.toLowerCase.split(" ") match {
      case Array(_, "start") => channelAction(from, to, message, bot)(startCards)
      case Array(_, "join") => gameAction(from, to, message, bot)(joinGame)
      case Array(_, "go") => gameAction(from, to, message, bot)(startGame)
      case Array(_, "stop") => gameAction(from, to, message, bot)(stopGame)
      case Array(_, number) => getChannel(from).map(selectCards(from, to, message, bot, _, number)).getOrElse(Seq.empty)
      case Array(_, channel, "select", number) => selectCards(from, to, message, bot, channel, number)
      case _ => Seq.empty
    }

  def selectCards(from: MessageSource, to: String, message: String, bot: ActorRef, chan: String, number: String): Seq[String] = {
    getChannel(chan).map { channel =>
      games.get(channel).map { game =>
        number.toIntOpt.foreach { cardNumber =>
          if(game.czar == to && game.cardPickers.get(from.source).isEmpty && game.allPlayersHavePlayed) {
            game.playerAnswers.get(cardNumber).map { player =>
              val updatedPlayer = player.copy(points = player.points + 1)
              game.players.update(updatedPlayer.name, updatedPlayer)
              game.cardPickers.foreach { case(name, player) =>
                game.players.update(name, player.copy(
                  selectedCards = Seq.empty,
                  cards = player.backfillCards(cards.whiteCards, game.currentBlackCard.numBlanks)
                ))
              }

              val updatedGame = game.copy(
                currentBlackCard = game.nextBlackCard(cards.blackCards),
                playerAnswers = MutableMap.empty,
                czar = game.nextCzar().name
              )

              games.update(channel, updatedGame)
              updatedGame.stepGame(channel, bot)
            }
          } else {
            game.cardPickers.get(from.source).map { picker =>
              val numBlanks = game.currentBlackCard.numBlanks
              if(picker.selectedCards.size < numBlanks) {
                if(picker.validAnswer(cardNumber)) {
                  val updatedPicker = picker.copy(selectedCards = picker.selectedCards :+ cardNumber)
                  game.players.update(picker.name, updatedPicker)
                  if(updatedPicker.selectedCards.size < numBlanks) {
                    val message = s"Select ${numBlanks - updatedPicker.selectedCards.size} more card(s)"
                    bot ! Messages.PrivMsg(updatedPicker.name, message)
                  }
                }
              }
            }

            if(game.allPlayersHavePlayed) {
              game.selectAndPrintAnswers(channel, bot)
            }
          }
        }
      }
    }

    Seq.empty
  }

  def startGame(game: Game, channel: String, to: String, message: String, bot: ActorRef): Seq[String] =
    if(game.players.size < 1) {
      Seq("Must have more than 1 player to start the game.")
    } else {
      val updatedGame = game.copy(
        state = GameStates.Running,
        czar = game.players.head._1,
        currentBlackCard = game.nextBlackCard(cards.blackCards)
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
    if(game.state == GameStates.Init && !game.players.contains(to)) {
      game.players += to -> Player(to, Player.takeCards(cards.whiteCards))
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
