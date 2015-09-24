package net.node3.scalabot.plugins.cards

case class Player(val points: Int, val cards: Seq[Int], val selectedCards: Seq[Int])

object Player {
  def apply(): Player = Player(0, Seq.empty, Seq.empty)
}
