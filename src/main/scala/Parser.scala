package net.node3.scalabot

import org.parboiled2._

object PEGParser {
  val special = CharPredicate('-', '^', '_', '^', '[', ']', '\\', '`')
  val nick = CharPredicate('{', '}') ++ special
  val host = CharPredicate('.') ++ special
  val CRLF = CharPredicate('\r', '\n')
  val white = CharPredicate('\t', '\f') ++ CRLF
  def apply(input: ParserInput) = new PEGParser(input).InputLine.run()
}

class PEGParser(val input: ParserInput) extends Parser {
  def InputLine = rule{
    Message ~ EOI
  }

  def Prefix: Rule1[Tokens.Prefix] = rule{
    capture(ServerName | Nick) ~ optional('!' ~ capture(User)) ~
    optional('@' ~ capture(ServerName)) ~> (Tokens.Prefix(_, _, _))
  }

  def Command: Rule1[Tokens.Command] = rule {
    capture(oneOrMore(CharPredicate.Alpha) | 3.times(CharPredicate.Digit)) ~>
    Tokens.Command
  }

  def Message: Rule1[Tokens.Message] = rule{
    optional(':' ~ Prefix ~ Space) ~ Command ~ Params ~> (
      Tokens.Message(_, _, _))
  }

  def Params: Rule1[List[String]] = rule{
    optional(Space ~ oneOrMore(!':' ~ capture(oneOrMore(
      CharPredicate.Visible -- PEGParser.white))).separatedBy(' ')) ~ End ~> (
        (mid: Option[Seq[String]], end: Option[String]) => (mid, end) match{
          case (Some(m), Some(e)) => (m :+ e).toList
          case (Some(m), None) => m.toList
          case (None, Some(e)) => List(e)
          case _ => List()
        })
  }

  def End: Rule1[Option[String]] = rule{
    optional(Space ~ ':' ~ capture(oneOrMore(ANY)))
  }

  def Space = rule{ oneOrMore(' ') }
  def ServerName = rule{ oneOrMore(CharPredicate.AlphaNum ++ PEGParser.host) }
  def Nick = rule{ oneOrMore(CharPredicate.AlphaNum ++ PEGParser.nick) }
  def User = rule{ optional('~') ~ Nick }
}
