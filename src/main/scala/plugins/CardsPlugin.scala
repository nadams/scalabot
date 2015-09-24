package net.node3.scalabot.plugins

import scala.collection.immutable.Map
import scala.collection.mutable.{ Map => MutableMap }

import akka.actor.ActorRef
import com.typesafe.config.Config

import net.node3.scalabot.config.Conf
import net.node3.scalabot.{ Plugin, PluginHelper, MessageSource }
import net.node3.scalabot.data._

object GameStates extends Enumeration {
  type State = Value

  val Init, Running = Value
}

case class Player(val points: Int, val cards: Seq[Int], val selectedCards: Seq[Int])

object Player {
  def apply(): Player = Player(0, Seq.empty, Seq.empty)
}

class Game(
  val players: MutableMap[String, Player],
  val cards: Cards,
  var czar: String,
  var state: GameStates.State,
  var currentBlackCard: Int
)

object Game {
  def apply(): Game = new Game(MutableMap.empty, "", GameStates.Init)
}

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

class CardsPlugin extends Plugin with PluginHelper {
  type ChannelAction = (String, String, String, ActorRef) => Seq[String]

  override val messages = Map[String, MessageHandler]("cards" -> cards)
  private val channelRegex = """((#|&).+)""".r
  private val games = MutableMap[String, Game]()
  private val cards = loadCards(CardsConfig(Conf.config.getConfig("bot.cards")))

  def cards(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    message.toLowerCase.split(" ") match {
      case Array(_, "start", _*) => channelAction(from, to, message, bot)(startCards)
      case Array(_, "join", _*) => channelAction(from, to, message, bot)(joinGame)
      case Array(_, "go", _*) => ???
      case Array(_, channel, "select", number, _*) => Seq(channel, "select", number)
      case _ => Seq.empty
    }

  def stopGame(from: MessageSource, to: String, message: String, bot: ActorRef): Seq[String] =
    ???

  def joinGame(channel: String, to: String, message: String, bot: ActorRef): Seq[String] =
    games.get(channel).map { game =>
      if(game.state == GameStates.Init && !game.players.contains(to)) {
        game.players += to -> Player()
        Seq(s"$to has joined the game", "Type `cards go` to start the game.")
      } else Seq.empty
    }.getOrElse(Seq("There isn't a game currently running."))

  def startCards(channel: String, to: String, message: String, bot: ActorRef): Seq[String] =
    games.get(channel).map { existingChannel =>
      Seq(s"A game is already in progress for channel: $existingChannel.")
    }.getOrElse {
      games.put(channel, Game())
      Seq("Type `cards join` in this channel to join the game.", "Type `cards start` to start the game.")
    }

  private def channelAction(from: MessageSource, to: String, message: String, bot: ActorRef)(action: ChannelAction): Seq[String] =
    getChannel(from).map(action(_, to, message, bot)).getOrElse(Seq("This message must be performed on a channel."))


  private def getChannel(from: MessageSource): Option[String] = from.source match {
    case channelRegex(channel) => Some(channel)
    case _ => None
  }

  private def loadCards(path: String): Cards = {
    import org.json4s._
    import org.json4s.native.JsonMethods._

    implicit val formats = DefaultFormats

    def mergeCards(obj: org.json4s.JsonAST.JValue) = ((obj \ "classic") merge (obj \ "custom"))

    val file = scala.io.Source.fromFile(path)
    val lines = try file.getLines mkString "\n" finally file.close()
    val json = parse(lines)
    val blackCards = mergeCards((json \ "black")).extract[Seq[String]]
    val whiteCards = mergeCards((json \ "white")).extract[Seq[String]]

    Cards(blackCards.map(BlackCard(_)), whiteCards.map(WhiteCard(_))
  }
}
