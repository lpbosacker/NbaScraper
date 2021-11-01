package nbascrape

import play.api.libs.json._
import java.sql.Date

case class Player(
    name : String
  , url : String
  , isActive : Boolean
  , dob : Date
  , height : String
  , weight : String
  , position : String
  , shoots : String
  , college : String) {

  val json = Json.obj(
      "name" -> name
    , "url" -> url
    , "active" -> isActive
    , "birth_date" -> dob.toString
    , "height" -> height
    , "weight" -> weight
    , "position" -> position 
    , "shoots" -> shoots
    , "college" -> college
    ).toString
}

