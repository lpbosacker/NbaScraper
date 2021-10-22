
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

object Team {
  
  val teams : Array[Team] = Array(
      Team("ATL", "Atlanta Hawks")
    , Team("BOS", "Boston Celtics")
    , Team("BRK", "Brooklyn Nets")
    , Team("CHA", "Charlotte Hornets")
    , Team("CHI", "Chicago Bulls")
    , Team("CLE", "Cleveland Cavaliers")
    , Team("DAL", "Dallas Mavericks")
    , Team("DEN", "Denver Nuggets")
    , Team("DET", "Detroit Pistons")
    , Team("GSW", "Golden State Warriors")
    , Team("HOU", "Houston Rockets")
    , Team("IND", "Indiana Pacers")
    , Team("LAC", "Los Angeles Clippers")
    , Team("LAL", "Los Angeles Lakers")
    , Team("MEM", "Memphis Grizzlies")
    , Team("MIA", "Miami Heat")
    , Team("MIL", "Milwaukee Bucks")
    , Team("MIN", "Minnesota Timberwolves")
    , Team("NOH", "New Orleans Pelicans")
    , Team("NYK", "New York Knicks")
    , Team("OKC", "Oklahoma City Thunder")
    , Team("ORL", "Orlando Magic")
    , Team("PHI", "Philadelphia 76ers")
    , Team("PHO", "Phoenix Suns")
    , Team("POR", "Portland Trail Blazers")
    , Team("SAC", "Sacramento Kings")
    , Team("SAS", "San Antonio Spurs")
    , Team("TOR", "Toronto Raptors")
    , Team("UTA", "Utah Jazz")
    , Team("WAS", "Washington Wizards")
  )

}

