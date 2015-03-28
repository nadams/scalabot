name := "Scala Bot"

organization := "net.node3"

version := "0.0.1"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.4" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.3.8",
  "org.parboiled" %% "parboiled" % "2.1.0",
  "com.typesafe" % "config" % "1.2.1",
  "com.github.kxbmap" %% "configs" % "0.2.3",
  "com.typesafe.play" %% "anorm" % "2.4.0-M2",
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
  "com.github.nscala-time" %% "nscala-time" % "1.8.0",
  "com.roundeights" %% "hasher" % "1.0.0"
)

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "RoundEights" at "http://maven.spikemark.net/roundeights"

initialCommands := "import net.node3.scalabot._"
