package net.node3.scalabot.plugins.cards

case class Player(val points: Int, val cards: Seq[WhiteCard], val selectedCards: Seq[Int]) {
  def cardsToString(): String = cards.zipWithIndex.foldLeft("") { case (acc, (content, index)) =>
    s"$acc ${index + 1}) ${content.content}"
  }
}

object Player {
  private val random = new scala.util.Random

  def apply(): Player = Player(0, Seq.empty, Seq.empty)
  def apply(cards: Seq[WhiteCard]): Player = Player(0, cards, Seq.empty)
  def takeCards(cards: Seq[WhiteCard], numCards: Int = 10): Seq[WhiteCard] =
    Stream.continually(random.nextInt(cards.length)).take(numCards).map(cards(_))
}
