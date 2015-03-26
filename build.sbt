name := "Scala Bot"

organization := "net.node3"

version := "0.0.1"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.4" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.3.8",
  "org.parboiled" %% "parboiled" % "2.1.0",
  "com.typesafe" % "config" % "1.2.1"
)

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

initialCommands := "import net.node3.scalabot._"
