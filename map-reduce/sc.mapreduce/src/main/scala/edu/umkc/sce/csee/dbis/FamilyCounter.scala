package edu.umkc.sce.csee.dbis

import scala.collection.mutable.ListBuffer

import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Set
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;


import java.util.Arrays
import org.apache.hadoop.io.Text;



object FamilyCounter {

  implicit def toNiceObject[T <: AnyRef](x : T) = new {
def niceClass : Class[_ <: T] = x.getClass.asInstanceOf[Class[T]]
}

  def bulkCountFamilies(f_filename: String, t_filename: String, K: Int, master: String, variables: Seq[String],  outfname: String, collect: Boolean):  Array[String] = {

    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    // initialize spark context
    val conf = new SparkConf().setAppName("FamilyCounter").setMaster(master)
    val sc = new SparkContext(conf)

    // get families of size <= K
    val allfamlilies = sc.textFile(f_filename, 2) // 2 minimum partitions 
    //val kfamilies = allfamlilies.map { x => x.split(",") }.filter { x => x.length <= K }.collect() // keep families with at most K size
    val kfamilies = allfamlilies.map { x => x.split(",") }.collect() // keep families with at most K size

    // bulk read input table
    val table = sc.newAPIHadoopFile(t_filename,classOf[NLinesInputFormat], classOf[LongWritable],classOf[Text])
   
    //sc.hadoopConfiguration.set("textinputformat.record.delimiter", "\n\n")
    //var table = sc.textFile(t_filename, 2) // 2 minimum partitions

    // store the column number of each variable in a map
    val vmap = scala.collection.mutable.Map[String, Integer]()
    variables.zipWithIndex.foreach { case (v, i) => vmap += (v -> i*2) }
    val varMap = sc.broadcast(vmap) // make the map available at every worker

    // for every block of rows calculate all possible family counts 
    val y = table.map { block =>
      {
        //initialize block family counters
        val counter_map = scala.collection.mutable.Map[Array[String], Array[Array[Int]]]() // to store a counter for each family
        kfamilies.foreach { fam =>{
            val length = 2
            val width = scala.math.pow(2, fam.length - 1).toInt // fam.length is K
            val counts = emptyCounter(length, width)
            // insert initial family counters into map
            counter_map += (fam -> counts)
            }
          }
        
          // count the families in this block
          val rows = block._2.toString().split("\n")
          val counters = new ListBuffer[(String, Array[Array[Int]])]() // to store the counter for each family
          kfamilies.foreach { fam =>
            {
              val fam_counter = counter_map.get(fam).get

              rows.foreach { row =>
                {
                  val col = row//.split(",")
                  val child = if (col.charAt(varMap.value.get(fam(0)).head)=='1') 1 else 0
                  //     parent(s) location (i.e. column)
                  val parents = getParentsLocationBits(col, varMap, fam)
                  // increment corresponding cell location
                  fam_counter(child)(parents) += 1 // i.e. counts[child_loc][parents_loc] ++
                }
              }
            counters += new Tuple2(fam.deep.mkString(","), fam_counter)
          }
          }
         counters  
        }
    }
    val families = y.flatMap { x => x }
    val sums = families.reduceByKey((left, right) => sumOf(left, right))
    val sum_strs = sums.map {case (key, value) => key + ":\n["  + value(0).deep.mkString(",") + "]\n[" + value(1).deep.mkString(",") + "]"}
    
    var results : Array[String] = null
    if (collect == false)
      sum_strs.saveAsTextFile(outfname)
    else
      results = sum_strs.collect()
    
    // terminate spark context
    sc.stop()

    return results 
  }
  def countFamilies(f_filename: String, t_filename: String, K: Int, master: String, variables: Seq[String], outfname: String, collect :Boolean):  Array[String] = {

    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    // initialize spark context
    val conf = new SparkConf().setAppName("FamilyCounter").setMaster(master)
    val sc = new SparkContext(conf)

    // get families of size <= K
    val allfamlilies = sc.textFile(f_filename, 2) // 2 minimum partitions 
    //val kfamilies = allfamlilies.map { x => x.split(",") }.filter { x => x.length <= K }.collect() // keep families with at most K size
    val kfamilies = allfamlilies.map { x => x.split(",") }.collect() // keep families with at most K size
    
    // read input table 
    var table = sc.textFile(t_filename, 2)
  
    // store the column number of each variable in a map
    val vmap = scala.collection.mutable.Map[String, Integer]()
    variables.zipWithIndex.foreach { case (v, i) => vmap += (v -> i*2) }
    val varMap = sc.broadcast(vmap) // make the map available at every worker

    
    // for every row calculate all possible family counts 
    val y = table.map { row =>
      //convert the current row from string to an array r
      val col = row//.split(",")

      val counters = new ListBuffer[(String, Array[Array[Int]])]() // to store a counter for each family

      kfamilies.foreach { fam =>
        {

          //create a counter for this family
          // 2x2^k where k is the number of parents = K-1
          val length = 2
          val width = scala.math.pow(2, fam.length - 1).toInt // fam.length is K
          val counts = emptyCounter(length, width)

          // get the values for this column family

          //   get counter cell locations to retrieve the corresponding values
          //     child location (i.e. row)
          val child = if (col.charAt(varMap.value.get(fam(0)).head)=='1') 1 else 0 

          //     parent(s) location (i.e. column)
          val parents = getParentsLocationBits(col, varMap, fam)

          // increment corresponding cell location
          counts(child)(parents) += 1 // i.e. counts[child_loc][parents_loc] ++

          // add this counter (i.e. counts) to the list of counters
          counters += new Tuple2(fam.deep.mkString(","), counts)
        }
      }
      //emit counters, to be merged (i.e. reduced) later
      counters
    }

    val families = y.flatMap { x => x }
    val sums = families.reduceByKey((left, right) => sumOf(left, right))
    val sum_strs = sums.map {case (key, value) => key + ":\n["  + value(0).deep.mkString(",") + "]\n[" + value(1).deep.mkString(",") + "]"}
    
      var results : Array[String] = null
    if (collect == false)
      sum_strs.saveAsTextFile(outfname)
    else
      results = sum_strs.collect()
    
    // terminate spark context
    sc.stop()

    return results     
  }

  def getParentsLocation(column: String, variablesMap: Broadcast[scala.collection.mutable.Map[String, Integer]], family: Array[String]): Integer = {
    val parents = new StringBuilder(family.length - 1) // store binary location

    //   for every parent get its value
    var i = 1
    while (i < family.length) {
      parents.append(column.charAt(variablesMap.value.get(family(i)).head*2)) //append (valOf  ( colOf (fam(i)) ) ) // where fam(i) is the ith parent Xi
      i += 1
    }
    if (parents.length > 0)
      return Integer.parseInt(parents.toString(), 2)
    return 0

  }
  def getParentsLocationBits(column: String, variablesMap: Broadcast[scala.collection.mutable.Map[String, Integer]], family: Array[String]): Integer = {

    var parents = 0
    var p = 1
    while (p < family.length) {
      // append a bit=0 (i.e. shift left)
      parents = parents << 1;
      
      // if parent is 1, flip last added bit=0 to 1 
			if (column.charAt(variablesMap.value.get(family(p)).head) == '1') 
				parents = parents | 1; // e.g. 110 | (00)1 = 111      
      p += 1
    }
    return parents
  }
  
    
  

  def emptyCounter(length: Integer, width: Integer): Array[Array[Int]] = {
    val counter = Array.ofDim[Array[Int]](length)
    counter(0) = Array.ofDim[Int](width)
    counter(1) = Array.ofDim[Int](width)

    for {
      p <- 0 until length
      q <- 0 until width
    } counter(p)(q) = 0

    return counter
  }
  def sumOf(X: Array[Array[Int]], Y: Array[Array[Int]]): Array[Array[Int]] = {

    val length = 2
    val width = X(0).length
    val sum = Array.ofDim[Array[Int]](2)
    sum(0) = Array.ofDim[Int](width)
    sum(1) = Array.ofDim[Int](width)
    for {
      r <- 0 until length
      c <- 0 until width
    } sum(r)(c) = X(r)(c) + Y(r)(c)
    sum
  }
}