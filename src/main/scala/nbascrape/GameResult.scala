package nbascrape

case class GameResult(
    result: String
  , teamPts : Int
  , oppPts : Int
  , wins : Int
  , losses : Int
  , overtime: Boolean
  , gameStreak : String)


