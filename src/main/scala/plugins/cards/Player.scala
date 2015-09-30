package net.node3.scalabot.plugins.cards

case class Player(val name: String, val points: Int, val cards: Seq[WhiteCard], val selectedCards: Seq[Int]) {
  def cardsToString(): String = cards.zipWithIndex.foldLeft("") { case (acc, (content, index)) =>
    s"$acc ${index + 1}) ${content.content}"
  }

  def answerString(): String = selectedCards.foldLeft("") { case (acc, index) =>
    s"$acc ${cards(index).content}"
  }.trim

  def validAnswer(answer: Int): Boolean =
    answer > 0 && answer <= cards.length && !selectedCards.contains(answer)

  def backfillCards(allCards: Seq[WhiteCard], numCards: Int): Seq[WhiteCard] =
    cards.diff(selectedCards.map(cards(_))) ++ Player.takeCards(allCards, numCards)
}

object Player {
  private val random = new scala.util.Random

  def apply(): Player = Player("", 0, Seq.empty, Seq.empty)
  def apply(name: String, cards: Seq[WhiteCard]): Player = Player(name, 0, cards, Seq.empty)
  def takeCards(cards: Seq[WhiteCard], numCards: Int = 10): Seq[WhiteCard] =
    Stream.continually(random.nextInt(cards.length)).take(numCards).map(cards(_))
}
