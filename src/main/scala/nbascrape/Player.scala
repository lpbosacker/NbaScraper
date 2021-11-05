package nbascrape

import java.sql.Date

case class Player (
    name : String
  , url : String
  , isActive : Boolean
  , dob : Date
  , height : String
  , weight : String
  , position : String
  , shoots : String
  , college : String) 

