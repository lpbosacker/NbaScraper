
package scrapetest

import org.scalatest.funsuite.AnyFunSuite
import nbascrape.NbaScraper._
import nbascrape.Player
import com.typesafe.config.{Config, ConfigFactory}

class NbaScrapeTest extends AnyFunSuite {

  val testCfg = ConfigFactory.load("application.conf")

  val testName = testCfg.getString("player_name")
  val testURL = testCfg.getString("test_url")
  val testCurrentYear = testCfg.getInt("current_year")
  val testPastYear = testCfg.getInt("past_year")
  val testTeamId = testCfg.getString("test_team_id")

  test("Player url returns players") {
    assert(getPlayerURLsByLetter(testURL).size > 0)
  }

  test(s"Game logs returned for ${testPastYear.toString}") {

    val testPlayer = getPlayerURLsByLetter(testURL).
      filter(_.name == testName).head
    val games = getPlayerGames(testPlayer, testPastYear)
    assert(games.size == 50)
  }

  test(s"Single game stats from ${testURL} should return elements") {
    val testPlayer = getPlayerURLsByLetter(testURL).filter(_.name == testName)(0)
    val oneGame = selectPlayerGames(testPlayer.url, testPastYear)(3)
    val stats = getSingleGameStats(oneGame)
    assert(stats.size > 0)
  }
    
  test(s"Year ${testPastYear.toString} for ${testName} returns games") {
    val testPlayer = getPlayerURLsByLetter(testURL).
      filter(_.name == testName).head
    val seasonStats = getPlayerGames(testPlayer, testCurrentYear)
    assert(seasonStats.size > 0)
  } 
    
  test("Team retrieval returns 30 teams") {
    val teams = getTeams
    assert(teams.size == 30)
  }

  test("Print team json succeeds") {
    val atlanta = getTeams.filter(_.teamId == "ATL").head
    assert(atlanta.json.startsWith("""{"team_id":"ATL""""))
  }

  test("Retrieves player attributes") {
    val testPlayer = getPlayerURLsByLetter(testURL).
      filter(_.name == testName).head
    val playerAttributes = getPlayerAttributes(testPlayer.url)
    assert(playerAttributes.size == 6) 
  }

  test(s"One active player objects created for ${testName}") {
    val testPlayerURL = getPlayerURLsByLetter(testURL).
      filter(_.name == testName) 
    val players = testPlayerURL.
      map(u => new Player(u.name, u.url, u.isActive
        , getPlayerAttributes(u.url) ) )
    assert(players.size == 1)
  }

  test(s"Team results for ${testTeamId} ${testCurrentYear} returned") {
    val results = getTeamGameResults(testTeamId, testCurrentYear)
    assert(results.size > 0)
  } 
}
