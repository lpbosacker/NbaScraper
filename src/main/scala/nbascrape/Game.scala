package nbascrape

case class Game(
    teamId: String
  , year: Int
  , oppTeamId: String
  , dateGame: java.sql.Date
  , homeGame : Boolean
  ) 

