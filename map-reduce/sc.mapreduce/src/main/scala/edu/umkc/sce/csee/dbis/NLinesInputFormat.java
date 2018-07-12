package edu.umkc.sce.csee.dbis;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

/*
 * Source: https://github.com/ElliottFodi/Hadoop-Programs/blob/master/Hadoop%20multiline%20read/multiLineFormat.java
*  This class assigns the custom record reader to read two lines 
*  as the record reader to be used 
*/



public class NLinesInputFormat extends TextInputFormat {
	 @Override
	    public RecordReader<LongWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context) {
	        return new NLinesRecordReader();
	    }
        
}
