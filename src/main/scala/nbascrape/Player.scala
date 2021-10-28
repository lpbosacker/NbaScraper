
package nbascrape

import play.api.libs.json._

case class Player(val name : String, val url : String, val isActive : Boolean,
      val attr : Map[String, String]) {

  val json = Json.obj(
    "name" -> name,
    "url" -> url,
    "active" -> isActive,
    "attr" -> attr
    ).toString
}

