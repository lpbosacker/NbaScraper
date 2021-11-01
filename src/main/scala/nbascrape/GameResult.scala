package nbascrape

import play.api.libs.json._

case class GameResult(
    teamId: String
  , year: Int
  , oppTeamId: String
  , dateGame: String
  , homeGame : Boolean
  , result: String
  , overtime: Boolean
  , teamPts : Int
  , oppPts : Int
  , wins : Int
  , losses : Int
  , gameStreak : String) {

  val json = Json.obj(
      "team_id" -> teamId
    , "opp_team_id" -> oppTeamId
    , "year" -> year
    , "date_game" -> dateGame
    , "home_game" -> homeGame
    , "result" -> result
    , "overtime" -> overtime
    , "team_pts" -> teamPts
    , "opp_pts" -> oppPts
    , "wins" -> wins
    , "losses" -> losses
  ).toString
}

