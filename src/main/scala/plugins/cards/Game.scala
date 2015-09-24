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
    players.foreach { case (name, player) =>
      bot ! Messages.PrivMsg(name, player.cardsToString)
    }

  def sendQuestionToChannel(channel: String, bot: ActorRef): Unit =
    bot ! Messages.PrivMsg(channel, currentBlackCard.content)
}

object Game {
  def apply(): Game = new Game(MutableMap.empty, Cards(), "", GameStates.Init, BlackCard(""))
}

