package nbascrape

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import scala.jdk.CollectionConverters._
import com.typesafe.config.Config
import nbascrape.NbaScraper.PlayerURL
import nbascrape.{TeamGame, GameResult}

// --------------------------------------------------

class NbaScraper(config : Config) {

  val baseURL = config.getString("source_url")

  val playerSelector = config.getString("css_selector.player")

/* ========================== *
  case class PlayerURL (
    name : String, url : String, isActive : Boolean
    ) {
      override def toString() : String = 
        { if (isActive) "*" else " " } + f"${name} @ ${url}"  
    }
 * ========================== */
    
  // ---------------------------------------------------------
  def getPlayers(playerURLs : Array[PlayerURL]) : Array[Player] = {
    playerURLs.map(p => new Player(p.name, p.url, p.isActive,
        getPlayerAttributes(p.url) ) )
  }

  // ---------------------------------------------------------

  def getPlayerURLs(letters : Seq[Char]) : Array[PlayerURL] = {
    letters.map(ch => s"${baseURL}/players/${ch}/").
      // build individual player url and combine (flatMap)
    flatMap(getPlayerURLsByLetter _).toArray
  }
  // ---------------------------------------------------------
  def getPlayerURLsByLetter(url : String) : Array[PlayerURL] = {

    val html = Jsoup.connect(url).get().select(playerSelector).asScala
    html.map(elem => PlayerURL(
      elem.text,
      elem.select("a").attr("href"),
      elem.select("strong").hasText()
      ) ).toArray
  }
  // ---------------------------------------------------------

  def getPlayerGames(playerURL : PlayerURL, year : Int) :
     Array[PlayerGame] = {
    
    val gameElements = selectPlayerGames(playerURL.url, year)
    gameElements.map(game => getSingleGameStats(game) ).
      map(stats => new PlayerGame(playerURL.name, year, stats) )
  }

  // ---------------------------------------------------------
  // get array of single game elements for player and year
  // using Jsoup select - returns Elements for input to getSinglePlayerGame
  //
  def selectPlayerGames(url : String, year : Int) : 
      Array[org.jsoup.nodes.Element] = {

    val gameLogURL = baseURL + url.replace(".html",s"/gamelog/${year.toString}")

    val gameSelector = config.getString("css_selector.game")

    Jsoup.connect(gameLogURL).get().body().
      select(gameSelector).asScala.toArray 
  }

  // ---------------------------------------------------------
  // get single game map of stats for player/year/game
  def getSingleGameStats(game : org.jsoup.nodes.Element) : 
    Map[String, String] = {

    val gameStatSelector = config.getString("css_selector.gameStat")
    game.select(s"td[${gameStatSelector}]").asScala.
      map(el => ( el.attr(s"${gameStatSelector}"), el.text() ) ).
      toMap
  }

  // ---------------------------------------------------------
  // get base player info from player URL (shoots, position, dob...)
  def getPlayerAttributes(playerURL : String) : Map[String, String] = {

    val stats = scala.collection.mutable.Map[String, String]()

    val fullURL = baseURL + playerURL 

    // ---------------------------------------------------------
    // function to parse out postion and shooting from html text
    //
    import scala.util.matching.Regex

    def getPositionShoots(txt : String) : Map[String, String] = {
      val psPat = """Position: ([\s\w]+).*Shoots: (\w+).*""".r
      val ps = txt match {
        case psPat(pos, shoots) => Seq(pos.trim(), shoots.trim())
        case _ => Seq("","")
      }
      ( Seq("position","shoots").zip(ps) ).toMap
    }

    // ---------------------------------------------------------
    // find text "Shoots:" and parse out shoots and position
    val body = Jsoup.connect(fullURL).get().body()
    val txt = body.selectFirst("p:contains(Shoots:)").text()
    stats ++= getPositionShoots(txt)

    for (sn <- Seq("height","weight") ) {
      stats ++= Map(sn -> body.selectFirst(s"span[itemprop='${sn}']").text)
    }
    stats ++= 
      Map("dob" -> body.selectFirst("span[data-birth]").attr("data-birth") )
    
    val college = body.selectFirst("p:contains(College:)")

    stats ++= 
      Map("college" -> {
        if ( college == null ) "None"
        else college.text().replace("College: ","")
        } ) 

    stats.toMap
  }

  // ---------------------------------------------------------
  // deprecated : incorrect team id retrieved for Brooklyn Nets
  // get team abbreviation, long name and url from team URL
  // Abbreviation is key
  def getTeams : Array[Team] = {

    val teamURL = config.getString("team_url")

    Jsoup.connect(teamURL).get().body().
      select("table[id=teams_active]").
      select("th[data-stat=franch_name] a[href]").asScala.
      map(el => (el.attr("href").substring(7,10),el.text())).
      map{ case (cd, nm) => new Team(cd, nm) }.toArray
  }

  // ---------------------------------------------------------

  def writeJson(fname : String, json : Array[String]) : Unit = {
    import java.io.FileWriter
    val fw = new FileWriter(fname)
    json.foreach(s => fw.write(s + "\n") )
    fw.close()
  }
  
  // ---------------------------------------------------------

  def getAllGameResults(year: Int) : Array[GameResult] = {
    val teamIds = Team.teams.map(_.teamId) 
    teamIds.flatMap(teamId => getTeamGameResults(teamId, year) )
  }
  // ---------------------------------------------------------

  def getTeamGameResults(teamId : String, year : Int) : Array[GameResult] = {
    val scheduleURL = config.getString("source_url") +
      s"/teams/${teamId}/${year.toString}_games.html"

    // select jsoup elements for all games for this team and year
    val gameEl = Jsoup.connect(scheduleURL).get().body().
        select("tbody").select("tr:not(tr.thead)").asScala
    
    // -----------------------------------------------------
    // filter fn to select only games with results
    def hasResult(g : Element) : Boolean =
      g.select("td[data-stat=game_result]").text() != ""
    // -----------------------------------------------------
  
    gameEl.filter(hasResult _).map(el => 
      GameResult(
        TeamGame(teamId
          , year
          , el.select("td[data-stat=opp_name]").attr("csk").substring(0,3)
          , el.select("td[data-stat=date_game]").attr("csk")
          , el.select("td[data-stat=game_location]").text()
        )
        , el.select("td[data-stat=game_result]").text()
        , (el.select("td[data-stat=overtimes]").text() == "OT")
        , el.select("td[data-stat=pts]").text().toInt
        , el.select("td[data-stat=opp_pts]").text().toInt
        , el.select("td[data-stat=wins]").text().toInt
        , el.select("td[data-stat=losses]").text().toInt
        , el.select("td[data-stat=game_streak]").text()
      )
    ).toArray
  }
  // ---------------------------------------------------------
}

// ---------------------------------------------------------
// ---------- companion object -----------------------------
object NbaScraper {
  
  val teams : Array[Team] = Array(
      Team("ATL", "Atlanta Hawks")
    , Team("BOS", "Boston Celtics")
    , Team("BRK", "Brooklyn Nets")
    , Team("CHA", "Charlotte Hornets")
    , Team("CHI", "Chicago Bulls")
    , Team("CLE", "Cleveland Cavaliers")
    , Team("DAL", "Dallas Mavericks")
    , Team("DEN", "Denver Nuggets")
    , Team("DET", "Detroit Pistons")
    , Team("GSW", "Golden State Warriors")
    , Team("HOU", "Houston Rockets")
    , Team("IND", "Indiana Pacers")
    , Team("LAC", "Los Angeles Clippers")
    , Team("LAL", "Los Angeles Lakers")
    , Team("MEM", "Memphis Grizzlies")
    , Team("MIA", "Miami Heat")
    , Team("MIL", "Milwaukee Bucks")
    , Team("MIN", "Minnesota Timberwolves")
    , Team("NOH", "New Orleans Pelicans")
    , Team("NYK", "New York Knicks")
    , Team("OKC", "Oklahoma City Thunder")
    , Team("ORL", "Orlando Magic")
    , Team("PHI", "Philadelphia 76ers")
    , Team("PHO", "Phoenix Suns")
    , Team("POR", "Portland Trail Blazers")
    , Team("SAC", "Sacramento Kings")
    , Team("SAS", "San Antonio Spurs")
    , Team("TOR", "Toronto Raptors")
    , Team("UTA", "Utah Jazz")
    , Team("WAS", "Washington Wizards")
  )

  case class PlayerURL (
    name : String, url : String, isActive : Boolean
    ) {
      override def toString() : String = 
        { if (isActive) "*" else " " } + f"${name} @ ${url}"  
    }
    
  // ---------------------------------------------------------

  def writeJson(fname : String, json : Array[String]) : Unit = {
    import java.io.FileWriter
    val fw = new FileWriter(fname)
    json.foreach(s => fw.write(s + "\n") )
    fw.close()
  }
  
}
