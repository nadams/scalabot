package net.node3.scalabot.plugins.cards

import scala.collection.immutable.Map
import scala.util.Random

import akka.actor.ActorRef

import net.node3.scalabot.Messages
import net.node3.scalabot.Implicits._

object GameStates extends Enumeration {
  type State = Value

  val Init, Running = Value
}

case class Game(
  val players: Map[String, Player],
  val czar: String,
  val question: BlackCard,
  val playerAnswers: Map[Int, Player],
  val czarAnswer: Option[Int]
) {
  private val random = new Random

  def cardCzar(): Player = players(czar)

  def cardPickers(): Map[String, Player] = players.filterNot { case(name, player) =>
    name == czar
  }.toMap

  def nextBlackCard(cards: Seq[BlackCard]): BlackCard =
    cards(random.nextInt(cards.length))

  def sendCardsToPlayers(bot: ActorRef): Unit =
    players.filterNot { case (name, player) => name == czar }.foreach { case (name, player) =>
      sendQuestion(name, bot)
      bot ! Messages.PrivMsg(name, player.cardsToString)
    }

  def printCzar(recipient: String, bot: ActorRef): Unit =
    bot ! Messages.PrivMsg(recipient, s"$czar is now card czar")

  def sendQuestion(recipient: String, bot: ActorRef): Unit =
    bot ! Messages.PrivMsg(recipient, question.content)

  def sendScores(recipient: String, bot: ActorRef): Unit = {
    bot ! Messages.PrivMsg(recipient, "The game has ended, here are the scores:")
    players.toSeq.sortBy(_._2.points)(Ordering[Int].reverse).foreach { case (name, player) =>
      bot ! Messages.PrivMsg(recipient, s"$name: ${player.points}")
    }
  }

  def allPlayersHavePlayed(): Boolean = cardPickers.forall { case (name, player) =>
    player.selectedCards.length >= question.numBlanks && player.selectedCards.map(player.cards(_)).forall(!_.isBlank)
  }

  def selectAnswers(): Game =
    copy(playerAnswers = Random.shuffle(cardPickers.values).zipWithIndex.map(x => x._2 + 1 -> x._1).toMap)

  def nextCzar(): Player = {
    val playerValues = players.values.toArray
    val czarIndex = playerValues.indexOf(players(czar))
    playerValues((czarIndex + 1) % playerValues.length)
  }

  def stepGame(recipient: String, bot: ActorRef): Unit = {
    printCzar(recipient, bot)
    sendQuestion(recipient, bot)
    sendCardsToPlayers(bot)
  }

  def pickAnswer(answer: Int, cards: Cards): (Player, Game) = {
    val player = playerAnswers(answer)
    val winner = player.copy(points = player.points + 1)
    winner -> copy(
      players = players.updated(winner.name, winner) ++ cardPickers.values.map { player =>
        player.name -> player.copy(
          selectedCards = Seq.empty,
          cards = player.backfillCards(cards.whiteCards, question.numBlanks)
        )
      },
      question = nextBlackCard(cards.blackCards),
      playerAnswers = Map.empty,
      czar = nextCzar().name,
      czarAnswer = None
    )
  }

  def pickCard(picker: Player, message: String, bot: ActorRef): Game = {
    val numBlanks = question.numBlanks
    if(picker.lastAnswer.nonEmpty && picker.lastAnswer.forall(_.isBlank)) {
      val filledBlank = picker.lastAnswer.map(_.copy(message)).getOrElse(WhiteCard.blankCard)
      picker.selectedCards.lastOption.map { index =>
        copy(players = players.updated(picker.name, picker.copy(cards = picker.cards.updated(index, filledBlank))))
      }.getOrElse(this)
    } else if(picker.selectedCards.size < numBlanks) {
      message.toIntOpt.map { cardNumber =>
        val zeroedCardNumber = cardNumber - 1
        val updatedPicker = picker.copy(selectedCards = picker.selectedCards :+ zeroedCardNumber)
        val updatedGame = copy(players = players.updated(picker.name, updatedPicker))
        if(updatedPicker.cards(zeroedCardNumber).isBlank) {
          bot ! Messages.PrivMsg(picker.name, s"Send content to use for your blank card")
        } else if(updatedPicker.selectedCards.size < numBlanks) {
          val message = s"Select ${numBlanks - updatedPicker.selectedCards.size} more card(s)"
          bot ! Messages.PrivMsg(updatedPicker.name, message)
        }

        updatedGame
      }.getOrElse(this)
    } else this
  }
}

object Game {
  def apply(): Game = new Game(Map.empty, "", BlackCard(""), Map.empty, None)
}

