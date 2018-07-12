package org.apache.gossip.model;

import java.util.Map;

public class Family {
  public String familyStr;
  public int[] locations;
  public int counterWidth;
  public float[][] counter;

  public Family() {

  }

  public Family(String familyStr, Map<String, Integer> variableMap) {
    this.familyStr = familyStr;
    String[] members = familyStr.split(",");
    this.locations = new int[members.length];
    for (int m = 0; m < members.length; m++) {
      this.locations[m] = variableMap.get(members[m]) * 2; // (* 2 to skip commas)
    }
    counterWidth = (int) Math.pow(2, locations.length - 1);
    counter = new float[2][counterWidth];
   
    // Initialize c
    for (int row = 0; row < 2; row++)
      for (int col = 0; col < counterWidth; col++)
        counter[row][col] = 0.0f; // initial value
  }

  public Family getClone() {
    Family clone = new Family();
    clone.counterWidth = counterWidth;
    clone.familyStr = familyStr;
    clone.locations = locations;
    clone.counter = new float[2][counterWidth];
  
    for (int row = 0; row < 2; row++)
      for (int col = 0; col < counterWidth; col++)
        clone.counter[row][col] = 0.0f; // initial value

    return clone;
  }
}
