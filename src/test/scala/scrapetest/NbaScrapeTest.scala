
package scrapetest

import org.scalatest.funsuite.AnyFunSuite
import nbascrape.NbaScraper._
import nbascrape.Player

class NbaScrapeTest extends AnyFunSuite {

  val testName = "Karl-Anthony Towns"
  val testURL = "https://basketball-reference.com/players/t/"
  val testGameYear = 2021

  test("Player url returns players") {
    assert(getPlayersAtURL(testURL).size > 0)
  }

  test("Player url returns one correct player") {
    val tPlayers = getPlayersAtURL(testURL)
    assert(tPlayers.filter(_.name == testName).size == 1)
  }

  test("Game logs returned") {
    val testPlayer = getPlayersAtURL(testURL).filter(_.name == testName).head
    val games = getPlayerGames(testPlayer.url, testGameYear)
    assert(games.size > 0)
  }

  test("Single game stats should return 29 elements") {
    val testPlayer = getPlayersAtURL(testURL).filter(_.name == testName)(0)
    val oneGame = selectPlayerGames(testPlayer.url, testGameYear)(3)
    val stats = getSingleGameStats(oneGame)
    assert(stats.size == 29)
  }
    
  test(s"Year ${testGameYear.toString} for ${testName} returns 50 games") {
    val testPlayer = getPlayersAtURL(testURL).filter(_.name == testName)(0)
    val seasonStats = getPlayerGames(testPlayer.url, testGameYear)
    assert(seasonStats.size == 50)
  } 
    
  test("Team retrieval returns 30 teams") {
    val teams = getTeams
    assert(teams.size == 30)
  }

  test("Print team json succeeds") {
    val atlanta = getTeams.filter(_.teamCd == "ATL").head
    assert(atlanta.json.startsWith("""{"team_cd":"ATL""""))
  }

  test(s"One active player objects created for ${testName}") {
    val testPlayerURL = time { getPlayerURLs.filter(_.name == testName) }
    val players = testPlayerURL.map(u => new Player(u.name, u.url, u.isActive) )
    assert(players.size == 1)
  }
}
