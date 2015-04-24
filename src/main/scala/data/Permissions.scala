package net.node3.scalabot.data

object PermissionFlags {
  val User: Permissions = 1 << 0
  val Admin: Permissions = 1 << 1
  val Owner: Permissions = 1 << 2

  private def is(x: Permissions, y: Permissions): Boolean = (x & y) == x

  def isUser(flags: Permissions) = is(User, flags)
  def isAdmin(flags: Permissions) = is(Admin, flags) || is(Owner, flags)
  def isOwner(flags: Permissions) = is(Owner, flags)
  def isAll(flag: Permissions, flags: Seq[Permissions]) = flags.forall(f => is(f, flag))
  def isAny(flag: Permissions, flags: Seq[Permissions]) = flags.exists(f => is(f, flag))
}
