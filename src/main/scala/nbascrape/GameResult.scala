package nbascrape

import nbascrape.TeamGame

import play.api.libs.json._

case class GameResult(
    game : TeamGame
  , result: String
  , overtime: Boolean
  , teamPts : Int
  , oppPts : Int
  , wins : Int
  , losses : Int
  , gameStreak : String) {

  val json = Json.obj(
      "team_id" -> game.teamId
    , "opp_team_id" -> game.oppTeamId
    , "year" -> game.year
    , "date_game" -> game.dateGame
    , "game_location" -> game.location
    , "result" -> result
    , "overtime" -> overtime
    , "team_pts" -> teamPts
    , "opp_pts" -> oppPts
    , "wins" -> wins
    , "losses" -> losses
  ).toString
}

