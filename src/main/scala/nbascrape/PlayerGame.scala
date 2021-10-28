package nbascrape

import play.api.libs.json._

case class PlayerGame(val name : String, val year: Int
    , val gameStats : Map[String,String]) {
  
  val json = Json.obj(
    "name" -> name,
    "year" -> year,
    "stats" -> gameStats
    ).toString
}

