package nbascrape

import play.api.libs.json._

case class TeamGame(
    teamId: String
  , year: Int
  , oppTeamId: String
  , dateGame: String
  , location: String
  ) {
  val json = Json.obj(
      "team_id" -> teamId
    , "opp_team_id" -> oppTeamId
    , "year" -> year
    , "date_game" -> dateGame
    , "location" -> location
  )
}
 
