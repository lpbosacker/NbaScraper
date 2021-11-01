package nbascrape

import play.api.libs.json._

case class TeamGame(
    teamId: String
  , year: Int
  , oppTeamId: String
  , dateGame: String
  , homeGame : Boolean
  ) {
  val json = Json.obj(
      "team_id" -> teamId
    , "opp_team_id" -> oppTeamId
    , "year" -> year
    , "date_game" -> dateGame
    , "home_game" -> homeGame
  )
}
 
