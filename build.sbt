
import sbt.Keys.libraryDependencies

ThisBuild / scalaVersion := "2.13.6"
ThisBuild / organization := "bosacker.link"

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
    

lazy val nbascrape = (project in file(".")).
  settings(
    name := "nbascrape",
    libraryDependencies ++= Seq(
       scalatest
      ,jsoup
      ,scalaXml
      ,typesafeConfig
      ,postgresql
      ,playjson
    )
  )

version := "0.1"
