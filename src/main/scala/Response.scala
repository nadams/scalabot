package net.node3.scalabot

import akka.util.{ ByteString, ByteStringBuilder }

trait Response {
  val byteString: ByteString

  def +(r: Response): ResponseCollection
}

class ResponseCollection(val responses: List[Response] = List()) extends Response {
  val byteString = responses.foldLeft(new ByteStringBuilder) {
    (acc, m) => acc ++= m.byteString ++= Chars.crlf
  }.result

  def +(r: Response) = new ResponseCollection(responses :+ r)
}

case class SimpleResponse(value: String) extends Response {
  val byteString = (new ByteStringBuilder ++= ByteString(value) ++= Chars.crlf).result

  def +(r: Response) = new ResponseCollection(List(this, r))
}
