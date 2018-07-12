package org.apache.gossip.manager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.gossip.model.Family;

public class FamilyCounterThread extends Thread {
  private final String filename;
  private final Family[] families;
  private final int blockSize;
  private final int numFamilies;



  public FamilyCounterThread(String filename, Family[] families, int blockSize) {
    this.filename = filename;
    this.families = families;
    this.blockSize = blockSize;
    numFamilies = families.length;
  }

  @Override
  public void run() {
    updateCounters();
  }

  private void updateCounters() {
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
      String[] lines;
      while ((lines = readBlock(br, blockSize)).length > 0) {
        int f = 0;
        while (f < numFamilies) {
          Family family = families[f++];
          float[][] family_counter = family.counter;
          for (int r = 0; r < lines.length; r++) {
            updateFamilyCount(family_counter, family.locations, lines[r]);
          }
        }
      }
    } catch (NumberFormatException e) {
      e.printStackTrace();
      System.exit(0);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  private void updateFamilyCount(float[][] counter, int[] familyLocations, String rowValues) {
    if (rowValues.length() < 1) {
      return;
    }
    // child variable column position
    int child_location = familyLocations[0];

    // get child location value
    // is variable at child_location is 1 or 0
    int child_counter_index = 0;
    if (rowValues.charAt(child_location) == '1')
      child_counter_index = 1;

    // get parents' counter column position
    int parents_counter_index = 0;
    for (int p = 1; p < familyLocations.length; p++) {
      // append a bit=0 (i.e. shift left)
      parents_counter_index = parents_counter_index << 1;

      // if parent is 1, flip last added bit=0 to 1
      if (rowValues.charAt(familyLocations[p]) == '1')
        parents_counter_index = parents_counter_index | 1;
      // e.g. pci = 110 | (00)1 = 111
    }
    counter[child_counter_index][parents_counter_index]++;

  }

  private String[] readBlock(BufferedReader br, int blockSize) {
    ArrayList<String> lines = new ArrayList<String>();
    int l = 0;
    String line;
    try {
      while (l < blockSize && (line = br.readLine()) != null) {
        lines.add(line);
        l++;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return lines.toArray(new String[lines.size()]);
  }

}
