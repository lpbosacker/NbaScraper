package nbascrape

import com.typesafe.config.{ConfigFactory, Config, ConfigException}
import scala.jdk.CollectionConverters._
import nbascrape.NbaScraper.PlayerURL

object Driver {
  
  val cfg = ConfigFactory.load("application.conf")
  
  val scraper = new NbaScraper(cfg)
  // sequence of characters to retrieve player URLs by first 
  // letter of player's last name
  val playerIdx : Seq[Char] = for (ch <- 'a' to 'z') yield { ch }

  lazy val playerURLs : Array[PlayerURL] = 
    scraper.getPlayerURLs(playerIdx).filter(_.isActive)

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

  commands.map(c => 
    c match {
      case "players" => writePlayers
      case "teams" => writeTeams
      case "player games" => writePlayerGames(cfg.getInt("season_year") )
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
    val games = playerURLs.flatMap(pu => scraper.getPlayerGames(pu, year))
    println(s"Retrieved ${games.size} player game stats")
    val getter = (pg : PlayerGame) => pg.json
    val jsonFile = cfg.getString("data_directory") +
      cfg.getString("player_game_file_name")
    (new DataWriter(jsonFile, getter) ).write(games)
  }

  // ---------------------------------------------------------

  def writePlayers : Unit = {
    val players = scraper.getPlayers(playerURLs)
    println(s"Retrieved ${players.size} players")
    val getter = (p : Player) => p.json
    val jsonFile = cfg.getString("data_directory") + 
      cfg.getString("player_file_name")
    (new DataWriter(jsonFile, getter) ).write(players)
        
  } 

  // ---------------------------------------------------------

  def writeTeams : Unit = {
    // val teams = scraper.getTeams
    val getter = (t : Team) => t.json
    val jsonFile = cfg.getString("data_directory") + cfg.getString("team_file_name")
    (new DataWriter(jsonFile, getter) ).write(Team.teams)
  }
  
  // ---------------------------------------------------------
}
