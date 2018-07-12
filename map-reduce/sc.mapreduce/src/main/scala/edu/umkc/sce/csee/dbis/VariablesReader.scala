package edu.umkc.sce.csee.dbis
import scala.collection.mutable.Set
import scala.util.control.Breaks._
import scala.io.Source
import scala.collection.mutable.ListBuffer

object VariablesReader {
  def getVariablesSeq(filename: String, max: Integer): Seq[String] = {

    val variables =   new ListBuffer[String]()
    var h = 0
    breakable {
      for (line <- Source.fromFile(filename).getLines()) {
        variables += line
        h += 1
        if (max != -1)
          if (h >= max)
            break
      }
    }
    return variables.toSeq
  }
}