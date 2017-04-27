import com.typesafe.sbt.packager.docker._

name := "Scala Bot"
organization := "net.node3"
version := "1.0.1"
scalaVersion := "2.11.8"
scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf8",
  "-feature",
  "-target:jvm-1.8"
)

scalacOptions in Test ++= Seq("-Yrangepos")

lazy val root = (project in file(".")).enablePlugins(JavaAppPackaging).enablePlugins(DockerPlugin).enablePlugins(AshScriptPlugin)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.3",
  "org.parboiled" %% "parboiled" % "2.1.0",
  "com.typesafe" % "config" % "1.3.0",
  "com.github.kxbmap" %% "configs" % "0.2.3",
  "com.typesafe.play" %% "anorm" % "2.4.0-M2",
  "com.github.nscala-time" %% "nscala-time" % "2.2.0",
  "com.github.t3hnar" %% "scala-bcrypt" % "2.4",
  "commons-lang" % "commons-lang" % "2.6",
  "commons-validator" % "commons-validator" % "1.4.1",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "org.ocpsoft.prettytime" % "prettytime" % "3.2.7.Final",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.slf4j" % "slf4j-simple" % "1.7.21",
  "net.ruippeixotog" %% "scala-scraper" % "1.0.0",
  "org.postgresql" % "postgresql" % "42.0.0"
)

dockerBaseImage := "openjdk:8-jre-alpine"
maintainer in Docker := "Nick Adams"
version in Docker := version.value
packageName in Docker := "docker.node-3.net:4567/nadams/scala-bot"

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)
resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  "NODE-3.net Snapshots" at "https://maven.node-3.net/repository/node-3-snapshots",
  "NODE-3.net Releases" at "https://maven.node-3.net/repository/node-3-releases"
)

initialCommands := "import net.node3.scalabot._"
bashScriptExtraDefines += """opts="$opts -Dconfig.file=${app_home}/../conf/application.conf -Dbot.db.migrations=${app_home}/../db""""
