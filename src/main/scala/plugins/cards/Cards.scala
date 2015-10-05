package net.node3.scalabot.plugins.cards

import scala.util.Random

import com.typesafe.config.Config
import org.apache.commons.lang.StringEscapeUtils

case class WhiteCard(val content: String) {
  def isBlank = content == WhiteCard.blankCard.content
}

object WhiteCard {
  val blankCard = WhiteCard("BLANK CARD")
}

case class BlackCard(val content: String) {
  def numBlanks: Int =
    content.foldLeft(0)((acc, char) => if(char == '_') acc + 1 else acc)
}

case class CardsConfig(val cardsPath: String, val numBlanks: Int)
object CardsConfig {
  def apply(c: Config): CardsConfig = CardsConfig(
    c.getString("cardsPath"),
    c.getInt("numBlanks")
  )
}

case class Cards(val blackCards: Seq[BlackCard], val whiteCards: Seq[WhiteCard])
object Cards {
  def apply(): Cards = Cards(Seq.empty, Seq.empty)
  def apply(path: String, numBlanks: Int): Cards = {
    import org.json4s._
    import org.json4s.native.JsonMethods._

    implicit val formats = DefaultFormats

    def mergeCards(obj: JValue) = ((obj \ "classic") merge (obj \ "custom"))

    def extractCards(json: JValue, key: String): Seq[String] =
      mergeCards((json \ key)).extract[Seq[String]]

    def fixContent(s: String): String =
      StringEscapeUtils.unescapeHtml(s.replace(" <br>", "."))

    val file = scala.io.Source.fromFile(path)
    val lines = try file.getLines mkString "\n" finally file.close()
    val json = parse(lines)
    val blackCards = Random.shuffle(extractCards(json, "black").map(x => BlackCard(fixContent(x))))
    val whiteCards = Random.shuffle(extractCards(json, "white").map(x => WhiteCard(fixContent(x))) ++ Seq.fill(numBlanks)(WhiteCard.blankCard))

    Cards(blackCards, whiteCards)
  }
}

