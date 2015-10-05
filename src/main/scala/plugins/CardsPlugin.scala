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

      println(gamesWithPlayer)

      if(gamesWithPlayer.size > 1) None
      else gamesWithPlayer.keySet.headOption
    }.map { channel =>
      games.get(channel).map { game =>
        if(game.czar == to && game.cardPickers.get(from.source).isEmpty && game.allPlayersHavePlayed) {
          number.toIntOpt.foreach { cardNumber =>
            game.playerAnswers.get(cardNumber).map { player =>
              val winner = player.copy(points = player.points + 1)
              game.players.update(winner.name, winner)
              game.cardPickers.foreach { case(name, player) =>
                game.players.update(name, player.copy(
                  selectedCards = Seq.empty,
                  cards = player.backfillCards(cards.whiteCards, game.question.numBlanks)
                ))
              }

              val updatedGame = game.copy(
                question = game.nextBlackCard(cards.blackCards),
                playerAnswers = MutableMap.empty,
                czar = game.nextCzar().name,
                czarAnswer = None
              )

              games.update(channel, updatedGame)
              bot ! Messages.PrivMsg(channel, s"${player.name} wins")
              updatedGame.stepGame(channel, bot)
            }
          }
        } else {
          game.cardPickers.get(from.source).map { picker =>
            val numBlanks = game.question.numBlanks
            if(picker.lastAnswer.nonEmpty && picker.lastAnswer.forall(_.isBlank)) {
              val filledBlank = picker.lastAnswer.map(_.copy(message)).getOrElse(WhiteCard.blankCard)
              picker.selectedCards.lastOption.foreach { index =>
                game.players.update(picker.name, picker.copy(cards = picker.cards.updated(index, filledBlank)))
              }
            } else if(picker.selectedCards.size < numBlanks) {
              number.toIntOpt.foreach { cardNumber =>
                val zeroedCardNumber = cardNumber - 1
                val updatedPicker = picker.copy(selectedCards = picker.selectedCards :+ zeroedCardNumber)
                game.players.update(picker.name, updatedPicker)
                if(updatedPicker.cards(zeroedCardNumber).isBlank) {
                  bot ! Messages.PrivMsg(picker.name, s"Send content to use for your blank card")
                } else if(updatedPicker.selectedCards.size < numBlanks) {
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
      Seq.empty
    }.getOrElse(Seq("Could not determine channel you're playing, select with the command `cards <channel> select <number>`"))

  def startGame(game: Game, channel: String, to: String, message: String, bot: ActorRef): Seq[String] =
    if(game.players.size < 2) {
      Seq("Must have more than 1 player to start the game.")
    } else {
      val updatedGame = game.copy(
        czar = game.players.head._1,
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
