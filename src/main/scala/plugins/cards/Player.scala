package net.node3.scalabot.plugins.cards

case class Player(val points: Int, val cards: Seq[WhiteCard], val selectedCards: Seq[Int]) {
  def cardsToString(): String = cards.zipWithIndex.foldLeft("") { case (acc, (content, index)) =>
    s"$acc ${index + 1}) ${content.content}"
  }
}

object Player {
  def apply(): Player = Player(0, Seq.empty, Seq.empty)
}
