package net.node3.scalabot.plugins.cards

import scala.collection.immutable.Map
import scala.collection.mutable.{ Map => MutableMap }
import scala.util.Random

import akka.actor.ActorRef

import net.node3.scalabot.Messages

object GameStates extends Enumeration {
  type State = Value

  val Init, Running = Value
}

case class Game(
  val players: MutableMap[String, Player],
  val cards: Cards,
  val czar: String,
  val state: GameStates.State,
  val currentBlackCard: BlackCard,
  val playerAnswers: MutableMap[Int, Player],
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
    bot ! Messages.PrivMsg(recipient, currentBlackCard.content)

  def sendScores(recipient: String, bot: ActorRef): Unit = {
    bot ! Messages.PrivMsg(recipient, "The game has ended, here are the scores:")
    players.toSeq.sortBy(_._2.points)(Ordering[Int].reverse).foreach { case (name, player) =>
      bot ! Messages.PrivMsg(recipient, s"$name: ${player.points}")
    }
  }

  def allPlayersHavePlayed(): Boolean = {
    val numBlanks = currentBlackCard.numBlanks
    cardPickers.forall { case (name, player) => player.selectedCards.length >= numBlanks }
  }

  def selectAndPrintAnswers(recipient: String, bot: ActorRef): Unit = {
    playerAnswers.empty
    playerAnswers ++= Random.shuffle(cardPickers.values).zipWithIndex.map(x => x._2 + 1 -> x._1)

    bot ! Messages.PrivMsg(recipient, s"$czar pick a card")
    playerAnswers.keys.toSeq.sortBy(x => x).foreach { key =>
      bot ! Messages.PrivMsg(recipient, s"$key) ${playerAnswers(key).answerString}")
    }
  }

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
}

object Game {
  def apply(): Game = new Game(MutableMap.empty, Cards(), "", GameStates.Init, BlackCard(""), MutableMap.empty, None)
}

