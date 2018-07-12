package edu.umkc.sce.cs.dbis.uniquehts;

/*
 * Anas Katib
 *
 * Read tweets from input directory and return a list of all unique 
 * hashtags in the directory.    
 * 
 * Assumes no nesting: input is a tweets directory that
 * contain tweets files.
 *
 * Usage: java -jar UniqueHts.jar tweets/
 *
 *
 *
 *
 *
 *
 *
 */

import java.nio.file.*;
import java.io.*;
import java.util.Iterator;
import java.util.HashSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class UniqueHts {

public static void printerr(Object obj){
    System.err.println(obj);
}


public static void println(Object obj){
    System.out.println(obj);
}
public static void  main (String [] args) throws Exception{
    HashSet<String> hashtags = new HashSet<String>();
    JSONParser parser = new JSONParser();

    printerr("Looking through: "+args[0]);
    File dir = new File(args[0]);

    File[] tweetsFiles = dir.listFiles();
    for (File file : tweetsFiles) {
            if (file.isFile()) {
                //printerr("Reading tweets from: "+file.getName());
                FileReader fileReader = new FileReader(file);
                BufferedReader br = new BufferedReader(fileReader);
                String line = null;
                while ((line = br.readLine()) != null){ 
                    try {
                        Object obj = parser.parse(line);
                        JSONObject job = (JSONObject) obj;
                        JSONArray hashtagArray = (JSONArray) job.get("hashtagEntities");
                        Iterator<JSONObject> iterator = hashtagArray.iterator();
                        while (iterator.hasNext()) {
                            hashtags.add((String)iterator.next().get("text"));
                        }
                    } catch (Exception e) {
                        // just skip it
                    }
            
                }// end while
                br.close();
            }//end if file
    }// end for file
    println("Number of unique hashtags:");
    println(hashtags.size());
    // print the hashtags
    Iterator iterator = hashtags.iterator();
    while (iterator.hasNext()){
        println(iterator.next());
    }
}
}
