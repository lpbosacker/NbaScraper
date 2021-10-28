
package nbascrape

import java.io.FileWriter

class DataWriter[T](fname : String, getr : T => String) { 
  

  // ------------------------------------------------------------------
  def withFileWriter[T](fname : String)(write : FileWriter => T) : T = {
    val writer = new FileWriter(fname)
    println(s"File $fname opened for writing")
    try {
      write(writer)
    } finally {
      writer.close()
    }
  }

  // ------------------------------------------------------------------

  def write(arr : Array[T]): Unit = {
    val sArr = arr.map(t => getr(t))
    withFileWriter(fname){ writer => 
      sArr.foreach(s => writer.write(s"$s\n"))
    }
  }
}
  // ---------------------------------------------------------
