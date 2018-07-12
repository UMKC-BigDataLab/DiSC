package org.apache.gossip.manager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.gossip.model.Family;



public class ParallelReader {

  private final String filename;
  private final int numSplits;
  private String[] fileSplits;
  private final int blockSize;

  public ParallelReader(String filename, int numSplits, int blockSize, boolean createSplits) {
    this.filename = filename;
    this.numSplits = numSplits;
    this.blockSize = blockSize;
    setupFileSplits(createSplits);
  }

  private void setupFileSplits(boolean create_splits) {
    fileSplits = new String[numSplits];
    PrintWriter[] splitWriter = new PrintWriter[numSplits];

    if (numSplits == 1) {
      fileSplits[0] = filename; // no need to split the file
      return;
    }

    for (int s = 0; s < numSplits; s++) {
      // prepare split name
      fileSplits[s] = filename + ".S-" + s;

      // create files to store splits
      if (create_splits) {
        try {
          splitWriter[s] = new PrintWriter(new FileWriter(fileSplits[s]));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    // write the input file's content into the splits
    if (create_splits) {
      try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;
        int splitNumber = 0;
        while ((line = br.readLine()) != null) {
          splitWriter[splitNumber].write(line + "\n");
          splitNumber++;
          if (splitNumber == numSplits) {
            splitNumber = 0;
          }
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      for (int s = 0; s < numSplits; s++) {
        splitWriter[s].close();
      }
    }
  }

  public void initCounterValues(Family[] families) {
    // Read the file splits via threads

    Thread[] threads = new Thread[numSplits];
    Family[][] threadFamilies = new Family[numSplits][];
    for (int s = 0; s < numSplits; s++) {
      threadFamilies[s] = cloneOf(families);
      Thread t = new FamilyCounterThread(fileSplits[s], threadFamilies[s], blockSize);
      t.start();
      threads[s] = t;
    }

    for (int t = 0; t < threads.length; t++) {
      try {
        threads[t].join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    
    for (int f = 0; f < families.length; f++) {
      for (int t = 0; t < numSplits; t++) {
        Family family = threadFamilies[t][f];
        for (int row = 0; row < 2; row++) {
          for (int col = 0; col < family.counterWidth; col++) {
            families[f].counter[row][col] += family.counter[row][col];
          }
        }
      }
    }
     
  }

  private Family[] cloneOf(Family[] families) {
    int numFams = families.length;
    Family[] cloned = new Family[numFams];
    for (int f = 0 ; f < numFams ; f++){
        cloned[f] = families[f].getClone();
    }
    return cloned;
  }
}
