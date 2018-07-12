package edu.umkc.sce.csee.dbis

import scala.collection.mutable.Set

object BooleanComboGenerator {
  def allPossible(len: Double): Set[String] = {
    val boolCombos = Set[String]()
    val n = Math.pow(2, len).toInt
    var x = 0
    
    while (x < n) {
      var i = n - 1
      val inst = new StringBuilder
      
      while (i > -1) {
        inst.append(((x >> i) & 1).toString())
         i -= 1
      }
      
      var splitPos = inst.length - len.toInt
        
      boolCombos.add(inst.splitAt(splitPos)._2.mkString(","))
      
      x += 1
    }
    return boolCombos
  }

  def randomTableOld(width: Double, length: Double): Array[String] = {
    val initVals = allPossible(width)
    val rlimit = initVals.size
    val vList = initVals.toSeq
    val r = scala.util.Random

    var table: Array[String] = Array()
    var i = 0
    while (i < length) {
      table :+= vList(r.nextInt(rlimit))
      i += 1
    }
    return table

  }
  def randomTable(width: Double, length: Double): Array[String] = {
    
    val table = Array.ofDim[String](length.toInt)
    var i = 0
    while (i < length) {
      table(i) = randomBoolStr(width.toInt)
      i += 1
    }
    return table

  }
  def randomBoolStr(size : Int): String = {
    val boolStr = new StringBuilder(size)
    
    val rand = scala.util.Random
    var c = 0
    while (c < size){
      val r = rand.nextInt(101)
      if (r < 51)
        boolStr.append("0")
      else
        boolStr.append("1")
      c += 1
    }
    return boolStr.mkString(",")
  }

}