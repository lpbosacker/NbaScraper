package nbascrape

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import com.typesafe.config.{ConfigFactory, Config, ConfigException}
import collection.JavaConverters._  // scala 2.12._ use JavaConverters
// scala 2.13 only -- import scala.jdk.CollectionConverters._
import com.typesafe.config.{Config, ConfigFactory}
import nbascrape.NbaScraper.PlayerURL
import java.sql.Date
import java.text.SimpleDateFormat
import scala.util.matching.Regex

// --------------------------------------------------

object NbaScraper {

  val cfg = ConfigFactory.load("application.conf")
  val baseURL = cfg.getString("source_url")

  case class PlayerURL (
    name : String, url : String, isActive : Boolean
    ) {
      override def toString() : String = 
        { if (isActive) "*" else " " } + f"${name} @ ${url}"  
    }
   
  val activeOnly = cfg.getBoolean("active_player_filter")

  val playerSelector = cfg.getString("css_selector.player")

  val teams = cfg.getObject("teams").entrySet().asScala.
    map(e => Team(e.getKey(), e.getValue().render() ) ).toList

  // ---------------------------------------------------------
  def toSQLDate(dateStr : String) : java.sql.Date = {
    val formatter = new SimpleDateFormat("yyyy-MM-dd")
    val utilDt = formatter.parse(dateStr)
    new java.sql.Date(utilDt.getTime())
  }
    
  // ---------------------------------------------------------
  def getPlayers(playerURLs : Array[PlayerURL]) : List[Player] = {

    playerURLs.map(p => {
      val attr = getPlayerAttributes(p.url)
      new Player(p.name, p.url, p.isActive
        , toSQLDate(attr("dob"))
        , attr("height"), attr("weight")
        , attr("position"), attr("shoots"), attr("college") )
      }
    ).toList
  }

  // ---------------------------------------------------------

  def getPlayerURLs(letters : Seq[Char]) : List[PlayerURL] = {
    
    letters.map(ch => s"${baseURL}/players/${ch}/").
      // build individual player url and combine (flatMap)
    flatMap(url => getPlayerURLsByLetter(url)).toList
  }
  // ---------------------------------------------------------
  def getPlayerURLsByLetter(url : String) : Array[PlayerURL] = {
    
    val playerFilter = {
      if (activeOnly) { (pURL : PlayerURL) => pURL.isActive }
      else { (pURL : PlayerURL) => true }
    }
    val html = Jsoup.connect(url).get().select(playerSelector).asScala
    html.map(elem => PlayerURL(
      elem.text,
      elem.select("a").attr("href"),
      elem.select("strong").hasText() // => isActive
      ) ).filter(playerFilter).toArray
  }

  // ---------------------------------------------------------

  def getPlayerGames(urls : List[PlayerURL], year : Int) : List[PlayerGame] = {
    urls.flatMap(u => getSinglePlayerGames(u, year) )
  }
  // ---------------------------------------------------------

  def getSinglePlayerGames(playerURL : PlayerURL, year : Int) :
     List[PlayerGame] = {
    
    // ---------------------------------------------------------
    // convert HH:MM string to float
      // pattern
    val HrMn = """\[(\d{1,2}):(\d{2})\]""".r
      // conversion function
    def mpToFloat(mp: String) : Float = {
      mp match {
        case HrMn(h,m) => h.toFloat + m.toFloat / 60.toFloat
        case _ => 0.toFloat
      }
    }
    // ---------------------------------------------------------
    def statsToPlayerGame(stats : Map[String, String]) : PlayerGame = {
        
      // -------------------------------------------------------
      // convert "" and missing integer stat fields to 0
      def nvl0(statName : String) : Int = {
        try {
          stats.getOrElse(statName,"0").toInt
        } catch {
          case ex : NumberFormatException => 0
        }
      }
      // -------------------------------------------------------
      val thisGame = Game(
          stats("team_id")
        , year
        , stats("opp_id")
        , toSQLDate(stats("date_game"))
        , (stats("game_location") != "@")
        )
      // add boolean to PlayerGame - dnp = stats("gs") is not defined
      PlayerGame(playerURL.name
        , thisGame
        , !(stats.contains("gs")) // dnp true if no stats found
        , pts = nvl0("pts")
        , fg = nvl0("fg")
        , fga = nvl0("fga")
        , fg3 = nvl0("fg3")
        , fg3a = nvl0("fg3a")
        , ft = nvl0("ft")
        , fta = nvl0("fta")
        , orb = nvl0("orb")
        , drb = nvl0("drb")
        , ast = nvl0("ast")
        , blk = nvl0("blk")
        , stl = nvl0("stl")
        , tov = nvl0("tov")
        , pf = nvl0("pf")
        , plus_minus = nvl0("plus_minus")
        , gs = nvl0("gs")
        , mp = mpToFloat(stats.getOrElse("mp","00:00"))
        , gameResult = stats.getOrElse("game_result","")

      )
    }

    // ---------------------------------------------------------
    val gameElements = selectPlayerGames(playerURL.url, year)
    gameElements.map(game => getSingleGameStats(game) ).
      map(stats => statsToPlayerGame(stats) )
  }

  // ---------------------------------------------------------
  // get array of single game elements for player and year
  // using Jsoup select - returns Elements for input to getSinglePlayerGame
  //
  def selectPlayerGames(url : String, year : Int) : 
      List[org.jsoup.nodes.Element] = {

    val gameLogURL = baseURL + url.replace(".html",s"/gamelog/${year.toString}")
    println(gameLogURL)
    val gameSelector = cfg.getString("css_selector.game")

    val session = Jsoup.newSession().timeout(30 * 1000)
    session.newRequest().url(gameLogURL).get().body().
      select(gameSelector).asScala.toList
  }

  // ---------------------------------------------------------
  // get single game map of stats for player/year/game
  def getSingleGameStats(game : org.jsoup.nodes.Element) : 
    Map[String, String] = {

    val gameStatSelector = cfg.getString("css_selector.gameStat")
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
  // select jsoup elements for all games for this team and year
  def getGameElements(teamId : String, year: Int) : List[Element] = {
    val scheduleURL = cfg.getString("source_url") +
      s"/teams/${teamId}/${year.toString}_games.html"
    Jsoup.connect(scheduleURL).get().body().
      select("tbody").select("tr:not(tr.thead)").asScala.toList
  }
  // ---------------------------------------------------------
  // map all temmIds to getTeam elmements filtered by presence of a result
  // create GameResult for each
  def getAllGames(year: Int) : List[TeamGame] = {
    val teamIds = teams.map(_.teamId)
    teamIds.flatMap(teamId => getTeamGames(teamId, year) ).toList
  }
  // ---------------------------------------------------------
  def getTeamGames(teamId : String, year : Int) : List[TeamGame] = {
    // -----------------------------------------------------
    // filter fn to select only games with results
    def hasResult(g : Element) : Boolean =
      g.select("td[data-stat=game_result]").text() != ""
    // -----------------------------------------------------

    getGameElements(teamId, year).map(el => 
        TeamGame(
            Game(
                teamId
              , year
              , el.select("td[data-stat=opp_name]").attr("csk").substring(0,3)
              , toSQLDate(el.select("td[data-stat=date_game]").attr("csk"))
              , (el.select("td[data-stat=game_location]").text() != "@" )
            )
          , if (hasResult(el)) {
              Some(
                GameResult(
                    el.select("td[data-stat=game_result]").text()
                  , el.select("td[data-stat=pts]").text().toInt
                  , el.select("td[data-stat=opp_pts]").text().toInt
                  , el.select("td[data-stat=wins]").text().toInt
                  , el.select("td[data-stat=losses]").text().toInt
                  , el.select("td[data-stat=overtimes]").text() == "OT"
                  , el.select("td[data-stat=game_streak]").text()
                )
              )
          } else {
            None
          }
        )
      )
  }
  // ---------------------------------------------------------

}

