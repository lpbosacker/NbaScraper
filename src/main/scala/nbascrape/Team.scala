
package nbascrape

import com.typesafe.config.{Config, ConfigFactory}
import play.api.libs.json._

case class Team (teamId : String, teamName : String) {

  val url = s"https://www.basketball-reference.com/teams/${teamId}/"

  val json = Json.obj(
    "team_id" -> teamId,
    "team_name" -> teamName,
    "team_url" -> url).toString

}
