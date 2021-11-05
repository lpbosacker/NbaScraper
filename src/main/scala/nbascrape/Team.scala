
package nbascrape

import com.typesafe.config.{Config, ConfigFactory}

case class Team (teamId : String, teamName : String) {

  val url = s"https://www.basketball-reference.com/teams/${teamId}/"
}
