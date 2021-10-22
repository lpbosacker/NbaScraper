
package dbtest

import org.scalatest.funsuite.AnyFunSuite
import com.typesafe.config.{Config, ConfigFactory}
import dbutil.DbUtil

class DbUtilTest extends AnyFunSuite {

  val testCfgName = "postgres_nba_local"

  test("Connection succeeded to local postgresql") {
    val c = DbUtil.getConnection()
    assert(c.isValid(5)) 
  }
}


