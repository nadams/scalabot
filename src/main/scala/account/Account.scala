package net.node3.scalabot.account

import net.node3.scalabot.Plugin
import net.node3.scalabot.db.DataCore

class AccountPlugin extends Plugin with DataCore {
  def apply(value: String) : Option[String] = Some("hello from account plugin")
}
