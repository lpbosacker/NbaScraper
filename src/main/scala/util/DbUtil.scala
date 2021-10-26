
package util

import com.typesafe.config.{Config, ConfigFactory, ConfigException}
import java.sql.{Connection, DriverManager}
// scala 2.13 only -- import scala.jdk.CollectionConverters._
// scala 2.12._ use JavaConverters
import collection.JavaConverters._
 
object DbUtil {

  // -------------------------------------------------

  def getConnection(configFileName : String = "database.conf") : Connection = {
    
    val config = ConfigFactory.load(configFileName)

    val url = config.getString("url")
    DriverManager.getConnection(url)
  }

  // -------------------------------------------------

}


