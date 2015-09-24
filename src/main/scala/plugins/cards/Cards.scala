package net.node3.scalabot.plugins.cards

import com.typesafe.config.Config
import org.apache.commons.lang.StringEscapeUtils

case class WhiteCard(val content: String)
case class BlackCard(val content: String) {
  private val blankRegex = """_""".r
  def numBlanks = blankRegex.findAllIn(content).size
}

case class CardsConfig(val cardsPath: String)
object CardsConfig {
  def apply(c: Config): CardsConfig = CardsConfig(
    c.getString("cardsPath")
  )
}

case class Cards(val blackCards: Seq[BlackCard], val whiteCards: Seq[WhiteCard])
object Cards {
  def apply(): Cards = Cards(Seq.empty, Seq.empty)
  def apply(path: String): Cards = {
    import org.json4s._
    import org.json4s.native.JsonMethods._

    implicit val formats = DefaultFormats

    def mergeCards(obj: org.json4s.JsonAST.JValue) = ((obj \ "classic") merge (obj \ "custom"))
    def fixContent(s: String): String =
      StringEscapeUtils.unescapeHtml(s.replace(" <br>", "."))

    val file = scala.io.Source.fromFile(path)
    val lines = try file.getLines mkString "\n" finally file.close()
    val json = parse(lines)
    val blackCards = mergeCards((json \ "black")).extract[Seq[String]]
    val whiteCards = mergeCards((json \ "white")).extract[Seq[String]]

    val c = Cards(blackCards.map(x => BlackCard(fixContent(x))), whiteCards.map(x => WhiteCard(fixContent(x))))
    println(c)

    c
  }
}

