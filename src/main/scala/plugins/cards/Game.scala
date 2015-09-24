package net.node3.scalabot.plugins.cards

import scala.collection.mutable.{ Map => MutableMap }

object GameStates extends Enumeration {
  type State = Value

  val Init, Running = Value
}

class Game(
  val players: MutableMap[String, Player],
  val cards: Cards,
  var czar: String,
  var state: GameStates.State,
  var currentBlackCard: BlackCard
)

object Game {
  def apply(): Game = new Game(MutableMap.empty, Cards(), "", GameStates.Init, BlackCard(""))
}

