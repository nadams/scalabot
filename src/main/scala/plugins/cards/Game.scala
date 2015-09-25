package net.node3.scalabot.plugins.cards

import scala.collection.mutable.{ Map => MutableMap }

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
  val currentBlackCard: BlackCard
) {
  private val random = new scala.util.Random

  def nextBlackCard(cards: Seq[BlackCard]) =
    cards(random.nextInt(cards.length))

  def sendCardsToPlayers(bot: ActorRef): Unit =
    players.filterNot { case (name, player) => name == czar }.foreach { case (name, player) =>
      bot ! Messages.PrivMsg(name, player.cardsToString)
    }

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
    players.forall { case (name, player) => player.selectedCards == numBlanks }
  }
}

object Game {
  def apply(): Game = new Game(MutableMap.empty, Cards(), "", GameStates.Init, BlackCard(""))
}

