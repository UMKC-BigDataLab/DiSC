package org.apache.gossip.model;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

public class DiscData implements Serializable {
  private static final long serialVersionUID = 1L;
  private String id;
  private DiscGossipMsgType type;
  private String UriFrom;
  public int R;
  public long timestamp;
  public HashMap<String, float[][][]> E;
  transient public HashMap<String, float[][]> FC;
  public BloomFilter familiesBf;
  
  public DiscData() {
  }
  
  
  public DiscData(ArrayList<Family> families, int R) {
    this.R = R;
    FC = new HashMap<String, float[][]>();
    E = new HashMap<String, float[][][]>();
    Iterator<Family> family = families.iterator();
    while (family.hasNext()) {
      Family f = family.next();
      FC.put(f.familyStr, f.counter);
    }
  }

  public DiscGossipMsgType getType() {
    return this.type;
  }

  public void setType(DiscGossipMsgType type) {
    this.type = type;
  }

  public String getFromId() {
    return id;
  }

  public void setFromId(String id) {
    this.id = id;
  }

  public void setUriFrom(String uri) {
    this.UriFrom = uri;
  }

  public String getUriFrom() {
    return this.UriFrom;
  }

  public String toString() {
    return "fNames: " + family_NamesToString() + " Es: " + EtoString();
  }

  public String family_NamesToString() {
    StringBuilder fnames = new StringBuilder();
    fnames.append("{");
    Iterator<String> families = E.keySet().iterator();
    while (families.hasNext()) {
      String family = families.next();
      fnames.append("\"" + family + "\"");
      if (families.hasNext())
        fnames.append(",");
    }
    fnames.append("}");
    return fnames.toString();
  }

  public String FCtoString() {
    StringBuilder FCs = new StringBuilder();
    FCs.append("{");
    Iterator<String> families = FC.keySet().iterator();
    while (families.hasNext()) {
      String family = families.next();
      FCs.append("\"" + family + "\":");
      FCs.append(Arrays.deepToString(FC.get(family)));
      if (families.hasNext())
        FCs.append(",");
    }
    FCs.append("}");
    return FCs.toString();
  }

  public String FCtoSeqString() {
    StringBuilder str = new StringBuilder();
    Iterator<String> families = FC.keySet().iterator();
    while (families.hasNext()) {
      String family = families.next();
      int N = (int) Math.pow(2, family.split(",").length - 1); // 2^k-1
      float[][] C = FC.get(family);
      str.append(family + ":");
      for (int r = 0; r < 2; r++)
        for (int c = 0; c < N; c++) {
          str.append(C[r][c]);
          if (r != 2 - 1 || c != N - 1)
            str.append(",");
        }
      str.append("|");
    }
    return str.toString();
  }

  public String EtoString() {
    StringBuilder Estr = new StringBuilder();
    Estr.append("{");
    Set<String> keys = E.keySet();
    int k = keys.size();
    for (String key : keys) {
      k--;
      Estr.append(key + " = " + Arrays.deepToString(E.get(key)));
      if (k > 0)
        Estr.append(", ");
    }
    Estr.append("}");
    return Estr.toString();
  }

  public int numFamilies() {
    return E.keySet().size();
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
  public long getTimestamp(){
    return timestamp;
  }

  public BloomFilter getBloomFamilies(){
    return familiesBf;
  }

  public void setBloomFamilies(BloomFilter bloomFamilies) {
    this.familiesBf = bloomFamilies;
    
  }
}
