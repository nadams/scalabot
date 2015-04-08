package net.node3.scalabot.plugins

import net.node3.scalabot.Plugin
import net.node3.scalabot.db.DataCore

class AccountPlugin extends Plugin with DataCore {
  def apply(from: String, to: String, message: String) : Option[String] = Some("hello from account plugin")
}
