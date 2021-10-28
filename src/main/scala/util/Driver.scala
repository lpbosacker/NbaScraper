package util

import com.typesafe.config.{ConfigFactory, Config, ConfigException}
// scala 2.13 only -- import scala.jdk.CollectionConverters._
import collection.JavaConverters._  // scala 2.12._ use JavaConverters
// import nbascrape.{NbaScraper, DataWriter, GameResult}
import nbascrape._

object Driver {
  
  val cfg = ConfigFactory.load("application.conf")
  
  // sequence of characters to retrieve player URLs by first 
  // letter of player's last name
  val playerIdx : Seq[Char] = for (ch <- 'a' to 'z') yield { ch }

  lazy val playerURLs : Array[NbaScraper.PlayerURL] = 
    NbaScraper.getPlayerURLs(playerIdx).filter(_.isActive)

  // --------- main ------------------------------------------

  def main(args : Array[String]) : Unit = {

  val commands = 
    try {
       cfg.getStringList("commands").asScala.toList
    } catch {
      case ex : ConfigException => {
        println("No commands found in configuration")
        List()
      }
    }

  val season_year = cfg.getInt("season_year")

  commands.map(c => 
    c match {
      case "players" => writePlayers
      case "teams" => writeTeams
      case "player games" => writePlayerGames(season_year)
      case "game results" => writeGameResults(season_year)
      case _ => println(s"Unknown command $c")
    }
  )
  } // end main ---------------------------------------------

  // ---------------------------------------------------------
  
  //
  def writePlayerGames(year : Int) : Unit = {
    
    println(
      s"Writing ${year.toString} game stats for ${playerURLs.size} players"
    )
    val games = playerURLs.flatMap(pu => NbaScraper.getPlayerGames(pu, year))
    println(s"Retrieved ${games.size} player game stats")
    val getter = (pg : PlayerGame) => pg.json
    val jsonFile = cfg.getString("data_directory") +
      cfg.getString("player_game_file_name")
    (new DataWriter(jsonFile, getter) ).write(games)
  }

  // ---------------------------------------------------------

  def writePlayers : Unit = {

    val players = NbaScraper.getPlayers(playerURLs)
    println(s"Retrieved ${players.size} players")
    val getter = (p : Player) => p.json
    val jsonFile = cfg.getString("data_directory") + 
      cfg.getString("player_file_name")
    (new DataWriter(jsonFile, getter) ).write(players.toArray)
        
  } 

  // ---------------------------------------------------------

  def writeTeams : Unit = {
    val getter = (t : Team) => t.json
    val jsonFile = cfg.getString("data_directory") + cfg.getString("team_file_name")
    (new DataWriter(jsonFile, getter) ).write(NbaScraper.teams.toArray)
  }
  
  // ---------------------------------------------------------

  def writeGameResults(year : Int) : Unit = {
    val results = NbaScraper.getAllGameResults(year).toArray
    val jsonFile = cfg.getString("data_directory") + 
        cfg.getString("game_results_file_name")
    val getter = (r : GameResult) => r.json
    (new DataWriter(jsonFile, getter )).write(results)
  }
}
