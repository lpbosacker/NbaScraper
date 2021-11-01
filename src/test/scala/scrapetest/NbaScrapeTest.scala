
package scrapetest

import org.scalatest.funsuite.AnyFunSuite
import nbascrape.NbaScraper._
import nbascrape.Player
import com.typesafe.config.{Config, ConfigFactory}
import collection.JavaConverters._

class NbaScrapeTest extends AnyFunSuite {

  val testCfg = ConfigFactory.load("application.conf")

  val testName = testCfg.getString("player_name")
  val testURL = testCfg.getString("test_url")
  val testCurrentYear = testCfg.getInt("current_year")
  val testPastYear = testCfg.getInt("past_year")
  val testTeamId = testCfg.getString("test_team_id")
  val testLetters = testCfg.getStringList("testLetters").asScala.map(_(0)).toSeq

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

  test("Player attribute selection succeeds") {
    val multiLetterURLs = getPlayerURLs(testLetters)
    val status = try {
      getPlayers(multiLetterURLs)
      true
    } catch {
      case ex : java.lang.NullPointerException => false
    }
  assert(status) 
  }

  test(s"Team results for ${testTeamId} ${testCurrentYear} returned") {
    val results = getTeamGameResults(testTeamId, testCurrentYear)
    assert(results.size > 0)
  } 
}
