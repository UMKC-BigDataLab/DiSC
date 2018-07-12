package edu.umkc.sce.csee.dbis

import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths

import scala.collection.mutable.Set

import org.apache.hadoop.fs.FSDataOutputStream
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object Main {

  def main(args: Array[String]) {

    printMemInfo

    val configfile = new File("configuration" + File.separator + "scMapReduce.conf")
    val config = ConfigFactory.parseFile(configfile)

    // Parse configuration file
 
    
    val use_hdfs = config.getBoolean("config.parameters.use_hdfs")
    val hadoop_master = config.getString("config.parameters.hadoop_master")
    val hadoop_config = new Configuration()
    var hdfs: FileSystem = null
    //input variables 
    var var_in  = config.getString("config.file_name.input.variables")
    // create output directory
    // output files in each run is different, us run id

    if (use_hdfs) {
      hadoop_config.set("fs.defaultFS", hadoop_master)
      System.setProperty("HADOOP_USER_NAME", "hduser")
      hdfs = FileSystem.get(hadoop_config);
    }

    val num_vars_to_read = config.getInt("config.parameters.num_vars")
    
    val max_fam_size = config.getInt("config.parameters.max_family_size")

    val generate_families = config.getBoolean("config.run.family_generator")
    val generate_table = config.getBoolean("config.run.table_generator")
    val count_families = config.getBoolean("config.run.family_counter")
    val bulk_count_families = config.getBoolean("config.run.bulk_family_counter")

    var variables: Seq[String] = scala.collection.mutable.Seq.empty
    if (generate_families || count_families || bulk_count_families) {
      System.err.println("Reading input variales..")
      variables = VariablesReader.getVariablesSeq(var_in, num_vars_to_read)
      if (variables.isEmpty == true) {
        System.err.println("Error: could not read variables!")
        return
      }
    }

    if (generate_table) {
      System.err.println("Generating input table..")
      val num_rows = config.getInt("config.parameters.num_rows")
      val tbl_out = config.getString("config.file_name.output.table")
      val btable = BooleanComboGenerator.randomTable(num_vars_to_read, num_rows) // N columns x M rows
      var tPW: PrintWriter = null
      var tHW: FSDataOutputStream = null
      if (use_hdfs)
        tHW = hdfs.create(new Path(tbl_out))
      else
        tPW = new PrintWriter(new File(tbl_out))

      val block = StringBuilder.newBuilder
      val capacity = 1000
      var line = 0
      var alllines = 0
      // write table rows
      btable.foreach { r =>
        {
          line += 1
          alllines += 1
          block.append(r + "\n")
          if (line == capacity || alllines == num_rows) {
            if (use_hdfs)
              tHW.write(block.toString().getBytes)
            else
              tPW.write(block.toString())
            line = 0
            block.setLength(0)
          }
        }
      }
      if (tHW != null)
        tHW.close()
      if (tPW != null)
        tPW.close()
    }
    if (generate_families) {
      System.err.println("Generating column families..")
      val families = ColumnFamilyGenerator.generateFamilies(variables, max_fam_size)
      val fam_out = config.getString("config.file_name.output.families")
      writeToFile(families,fam_out,use_hdfs,hdfs)
    }
    if (count_families || bulk_count_families) {
      val spark_master = config.getString("config.parameters.spark_master")
      val collect = config.getBoolean("config.parameters.collect")
      
      var families_filename = config.getString("config.file_name.input.families")
      var table_filename = config.getString("config.file_name.input.table")
      if (use_hdfs){
        families_filename = hadoop_master+families_filename
        table_filename = hadoop_master+table_filename
      }

      // Count the families      
      val fc = FamilyCounter
      if (count_families) {
        System.err.println("Counting families..")
        var cnt_out = config.getString("config.file_name.output.counts")
	if(use_hdfs)
        	cnt_out = hadoop_master + cnt_out 
        

        val start = System.currentTimeMillis()
        val counts = fc.countFamilies(families_filename, table_filename, max_fam_size, spark_master, variables, cnt_out,collect)
        val end = System.currentTimeMillis()

        System.err.println("Counted families in: " + (end - start) / 1000.0 + "s")
        if (counts != null) 
          writeToFile(counts, cnt_out, use_hdfs, hdfs)
      }

      if (bulk_count_families) {
        System.err.println("Bulk counting families..")
        var blk_cnt_out = config.getString("config.file_name.output.bulk_counts")
	if(use_hdfs)
        	blk_cnt_out = hadoop_master + blk_cnt_out 
        
        val start = System.currentTimeMillis()
        val counts = fc.bulkCountFamilies(families_filename, table_filename, max_fam_size, spark_master, variables, blk_cnt_out,collect)
        val end = System.currentTimeMillis()

        System.err.println("Bulk counted families in: " + (end - start) / 1000.0 + "s")
        if (counts != null) 
          writeToFile(counts, blk_cnt_out, use_hdfs, hdfs)
      }
    }
    if (hdfs != null)
      hdfs.close()
    printMemInfo
  }
  def writeToFile(data: Array[String], fname: String, use_hdfs : Boolean, hdfs: FileSystem): Unit = {
    var fPW: PrintWriter = null
    var fHW: FSDataOutputStream = null
    if (use_hdfs)
      fHW = hdfs.create(new Path(fname))
    else
      fPW = new PrintWriter(new File(fname))
    data.foreach { f => if (use_hdfs) fHW.write((f + "\n").getBytes) else fPW.write(f + "\n") }
    if (fHW != null)
      fHW.close()
    if (fPW != null)
      fPW.close()
  }
  def printMemInfo(): Unit = {
    System.err.println("Memory:")
    System.err.println("     Used: " + String.valueOf(sys.runtime.totalMemory() / 1073741824) + " gb")
    System.err.println("      Max: " + String.valueOf(sys.runtime.maxMemory() / 1073741824) + " gb")
    System.err.println("     Free: " + String.valueOf(sys.runtime.freeMemory() / 1073741824) + " gb")

  }
  
}
