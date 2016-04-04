name := "Scala Bot"

organization := "net.node3"

version := "1.0.0"

scalaVersion := "2.11.8"

lazy val root = (project in file(".")).enablePlugins(JavaAppPackaging)

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.4" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.4.3",
  "org.parboiled" %% "parboiled" % "2.1.0",
  "com.typesafe" % "config" % "1.3.0",
  "com.github.kxbmap" %% "configs" % "0.2.3",
  "com.typesafe.play" %% "anorm" % "2.4.0-M2",
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
  "com.github.nscala-time" %% "nscala-time" % "2.2.0",
  "com.roundeights" %% "hasher" % "1.0.0",
  "com.github.t3hnar" %% "scala-bcrypt" % "2.4",
  "commons-lang" % "commons-lang" % "2.6",
  "commons-validator" % "commons-validator" % "1.4.1",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "org.ocpsoft.prettytime" % "prettytime" % "3.2.7.Final"
)

scalacOptions in Test ++= Seq("-Yrangepos")

scalacOptions += "-deprecation"

scalacOptions += "-feature"

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "RoundEights" at "http://maven.spikemark.net/roundeights"

initialCommands := "import net.node3.scalabot._"

bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/application.conf""""

bashScriptExtraDefines += """addJava "-Ddb.dir=${app_home}/../db""""
