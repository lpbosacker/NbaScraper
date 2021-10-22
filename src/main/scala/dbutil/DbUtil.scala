
package dbutil

import com.typesafe.config.{Config, ConfigFactory, ConfigException}
import java.sql.{Connection, DriverManager}
import scala.jdk.CollectionConverters._
 
object DbUtil {

  // -------------------------------------------------

  def getConnection(configFileName : String = "database.conf") : Connection = {
    
    val config = ConfigFactory.load(configFileName)

    val url = config.getString("url")
    DriverManager.getConnection(url)
  }

  // -------------------------------------------------

}


