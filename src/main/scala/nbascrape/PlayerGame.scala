package nbascrape

case class PlayerGame(
      name : String
    , game : Game
    , dnp : Boolean
    , pts : Int
    , fg : Int
    , fga : Int
    , fg3 : Int
    , fg3a : Int
    , ft : Int
    , fta : Int
    , orb : Int
    , drb : Int
    , ast : Int
    , blk : Int
    , stl : Int
    , tov : Int
    , pf : Int
    , plus_minus : Int
    , gs : Int
    , mp : Float
    , gameResult : String
    ) 

/* ======================================== *

  {stl":"0"
"fg3_pct":""
"gs":"0"
"opp_id":"NOP"
"blk":"1"
"pts":"11"
"ft":"1"
"ft_pct":".500"
"ast":"2"
"game_result":"W (+13)"
"date_game":"2020-12-25"
"fta":"2"
"game_location":""
"fg_pct":".714"
"pf":"6"
"game_season":"2"
"team_id":"MIA"
"fg":"5"
"game_score":"7.7"
"orb":"1"
"fg3a":"0"
"fg3":"0"
"age":"21-097"
"plus_minus":"+4"
"drb":"2"
"mp":"19:28"
"tov":"1"
"trb":"3"
"fga":"7"}

 * ======================================== */

