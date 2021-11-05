
package nbascrape

case class TeamGame(
    game : Game
  , result : Option[GameResult]
  ) {

  val completed = result.isDefined
}

