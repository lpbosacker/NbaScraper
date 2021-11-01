
// import sbt.Keys.libraryDependencies

ThisBuild / scalaVersion := "2.12.10"
ThisBuild / organization := "bosacker.link"
ThisBuild / assemblyMergeStrategy  := {
  case PathList("module-info.class") => MergeStrategy.discard
  case x if x.endsWith("/module-info.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

Compile / run / fork := true
Test / fork := true

scalacOptions ++= Seq("-deprecation", "-feature")

//
// Dependencies
//
val scalatest = "org.scalatest" %% "scalatest" % "3.2.10" % Test
val jsoup = "org.jsoup" % "jsoup" % "1.14.2"
val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "2.0.1"
val typesafeConfig = "com.typesafe" % "config" % "1.4.1"
val postgresql = "org.postgresql" % "postgresql" % "42.2.24"
val playjson = "com.typesafe.play" %% "play-json" % "2.10.0-RC5"
// val nscalatime = "com.github.nscala-time" %% "nscala-time" % "2.30.0"

lazy val nbascrape = (project in file(".")).
  settings(
    name := "main",
    assembly / assemblyJarName := "nbascrape.jar",
    libraryDependencies ++= Seq(
       scalatest
      ,jsoup
      ,typesafeConfig
      ,postgresql
      ,playjson
    )
  )

