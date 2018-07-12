package edu.umkc.sce.csee.dbis

import scala.collection.mutable.Set
import scala.collection.mutable.ListBuffer

/**
 * @author Anas Katib
 */
object ColumnFamilyGenerator {
  def powerset[A](s: Seq[A], f: Int) = (0 to f).map(s.combinations(_)).reduce(_ ++ _).map(_.toSet)

  def printerr(obj: Any): Unit = {
    System.err.println(obj)
  }

  def generateFamilies(variables: Seq[String], max_fam_len: Integer): Array[String] = {

    val fams = powerset(variables, max_fam_len)
    val del = ","
    val families = new ListBuffer[String]()

    fams.foreach(x => ({
      x.foreach(y => ({
        var s = x.-(y)
        //add the variable
        val fam = new StringBuilder
        fam.append(y.toString())

        //add the parents
        var i = 0
        if (s.size > 0) // if there are parents
          s.toSeq.sorted.foreach(x => {
            fam.append("," + x.toString())
          })

        families += fam.toString()
      }))

    }))
    return families.toArray
  }

}
