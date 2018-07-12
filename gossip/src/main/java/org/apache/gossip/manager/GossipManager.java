/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.gossip.manager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.apache.gossip.GossipMember;
import org.apache.gossip.GossipService;
import org.apache.gossip.GossipSettings;
import org.apache.gossip.LocalGossipMember;
import org.apache.gossip.event.GossipListener;
import org.apache.gossip.event.GossipState;
import org.apache.gossip.manager.impl.OnlyProcessReceivedPassiveGossipThread;
import org.apache.gossip.model.AlgorithmType;
import org.apache.gossip.model.DiscData;
import org.apache.gossip.model.DiscDataHeader;
import org.apache.gossip.model.Family;
import org.apache.gossip.model.GossipDataMessage;
import org.apache.gossip.model.SharedGossipDataMessage;
import org.apache.log4j.Logger;


public abstract class GossipManager implements NotificationListener {

  public static final Logger LOGGER = Logger.getLogger(GossipManager.class);

  public static final int MAX_PACKET_SIZE = 65507;
  public final double AGT_PACKING_FACTOR; //syn 1, higgs 3, twtr 2
  public final int PGT_EXTRACTION_SIZE;  //135000 for twtr 140000 for higgs 130000 for syn 

  private final ConcurrentSkipListMap<LocalGossipMember, GossipState> members;

  private final LocalGossipMember me;

  private final GossipSettings settings;

  private final AtomicBoolean gossipServiceRunning;

  private final GossipListener listener;

  public ActiveGossipThread activeGossipThread;

  private PassiveGossipThread passiveGossipThread;
  private DiscDataProcessorThread dataProcessorThread;

  private ExecutorService gossipThreadExecutor;

  private final GossipCore gossipCore;

  private final DataReaper dataReaper;

  private final Clock clock;

  private DiscData data;

  private final String variablesFilename;
  private final String familiesFilename;
  private final int R;
  public final int logLimit;
  public final int P1;
  public final int P2;
  private final boolean use_stored_vals;
  private final int block_size;
  private final int num_read_splits;
  private final boolean create_splits;
  private String[] initCountVals;
  private final int stopTime;
  public final boolean gossip_all_families;
  public HashSet<String> managerVisibleFamilies;
  public HashSet<String> managerPermanentFamilies;
  public HashMap<String, Integer> familyGossipCount;
  public HashMap<String, Integer> allGossipCount;
  public HashMap<String, Integer> variables;
  public int numberOfVisibleFamilies;
  public ConcurrentLinkedQueue<DiscDataHeader> responseQueue;
  
  public boolean isManagerReady = false;
  public boolean compressDiscData;
  public long clockTick;
  public HashMap<String, Long> lastReceived;

  private double delay_constant;
  public String id;
  private String [] uris;
  private String [] membersFamilies;
  

  public AlgorithmType type;

  private final String tableFilename;
  private final List<GossipMember> backupGossipMembersList;
  public String mutex;

  public GossipManager(String cluster, URI uri, String id, GossipSettings settings,
      List<GossipMember> gossipMembers, GossipListener listener) {
    // Runtime runtime = Runtime.getRuntime();
    // printMemory(runtime);
    // Read Configuration File
    Properties prpts = null;
    try {
      FileInputStream inputStream = new FileInputStream("configuration/config.txt");
      InputStreamReader isr = new InputStreamReader(inputStream, "UTF-8");
      prpts = new Properties();
      prpts.load(isr);
      isr.close();
      inputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (prpts == null) {
      System.err.println("Configuration File Error");
    }
    this.delay_constant = Double.parseDouble(prpts.getProperty("delay_const"));
    this.mutex = "MUTEX";
    this.id = id;
    this.settings = settings;
    this.compressDiscData = Boolean.parseBoolean(prpts.getProperty("CompressDiscData"));
    responseQueue = new ConcurrentLinkedQueue<DiscDataHeader>();
    this.clockTick = 0L;
    this.lastReceived = new HashMap<String, Long>();
    
    clock = new SystemClock();
    
    me = new LocalGossipMember(cluster, uri, id, System.currentTimeMillis(), this,
        settings.getCleanupInterval());
    backupGossipMembersList = gossipMembers;
    members = new ConcurrentSkipListMap<>();
    gossipThreadExecutor = Executors.newCachedThreadPool();
    gossipServiceRunning = new AtomicBoolean(true);
    this.listener = listener;
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      public void run() {
        synchronized (mutex) {
        // print last estimates
        LOGGER.info("DiSC: Node (" + id + ") EST C = " + getDiscData().FCtoString());
        }

        // print number of times families has been gossiped
        StringBuilder str = new StringBuilder();
        Iterator<String> fgcItr = allGossipCount.keySet().iterator();
        int f = 0;
        boolean hasNext = fgcItr.hasNext();
        while (hasNext) {
          String fname = fgcItr.next();
          str.append(f + ":" + allGossipCount.get(fname));
          hasNext = fgcItr.hasNext();
          if (hasNext) {
            str.append(", ");
            f++;
          }
        }
        LOGGER.info("DiSC: Node (" + id + ") Families Gossip Counts = [" + str.toString() + "]");
        GossipService.LOGGER.debug("Service has been shutdown...");
      }
    }));

    String atype = prpts.getProperty("AlgorithmType");
    if (atype.equals(AlgorithmType.Sum.name()))
      this.type = AlgorithmType.Sum;
    else if (atype.equals(AlgorithmType.Ave.name()))
      this.type = AlgorithmType.Ave;
    else {
      System.err.println("Configuration File: unknown algorithm [" + atype + "]");
      this.type = AlgorithmType.Ave;
    }
    LOGGER.info("DiSC: Node (" + id + ") Algorithm Type:" + this.type.name());

    uris = prpts.getProperty("IDS").split(";");
    variablesFilename = prpts.getProperty("variables_file");
    familiesFilename = prpts.getProperty("family_file");
    R = Integer.parseInt(prpts.getProperty("R"));
    logLimit = Integer.parseInt(prpts.getProperty("EstLogLimit"));
    P1=Integer.parseInt(prpts.getProperty("P1"));
    P2=Integer.parseInt(prpts.getProperty("P2"));
    AGT_PACKING_FACTOR = Double.parseDouble(prpts.getProperty("PackingFactor"));     
    PGT_EXTRACTION_SIZE = Integer.parseInt(prpts.getProperty("ExtractionSize"));
    LOGGER.info("Node (" + id + ") R is set to " + R);
    use_stored_vals = Boolean.parseBoolean(prpts.getProperty("ReadStoredVals"));
    block_size = Integer.parseInt(prpts.getProperty("BlockSize"));
    num_read_splits = Integer.parseInt(prpts.getProperty("NumberOfTableSplits"));
    create_splits = Boolean.parseBoolean(prpts.getProperty("CreateSplits"));
    gossip_all_families = Boolean.parseBoolean(prpts.getProperty("GossipAllFamilies"));
    if (gossip_all_families) {
      numberOfVisibleFamilies = 0;
    } else {
      
      membersFamilies = new String [uris.length];
      String[] vFamilies = null;
      for (int i = 0; i < uris.length; i++){
        // e.g. uris[i] = 192.168.0.104:20000,1-0
        
        String [] uriInfo =  uris[i].split(",");
        uris[i] =  "udp://"+uriInfo[0]; // udp://192.168.0.104:20000
        membersFamilies[i] = prpts.getProperty("Node_" + uriInfo[1] + "_gspfams");
        
        if (uriInfo[1].equals(id)){
          vFamilies = membersFamilies[i].split(";");
        }
      }
      
       
      // get which families to gossip
      managerVisibleFamilies = hashSetOf(vFamilies);
      managerPermanentFamilies = hashSetOf(vFamilies);
      numberOfVisibleFamilies = managerVisibleFamilies.size();
    }
    familyGossipCount = new HashMap<String, Integer>();
    allGossipCount = new HashMap<String, Integer>();
    tableFilename = prpts.getProperty("table_file");
    if (use_stored_vals == true) {
      initCountVals = prpts.getProperty("Node_" + id + "_vals").split("\\|");
    }
    stopTime = Integer.parseInt(prpts.getProperty("StopTime"));
    gossipCore = new GossipCore(this);
    dataReaper = new DataReaper(gossipCore, clock);
    // managerIsReady = false;
  }

  

  private void printMemory(Runtime runtime) {
    System.out.println("Memory:");
    System.out.println("     Init: " + String.valueOf(runtime.totalMemory() / 1073741824) + " gb");
    System.out.println("      Max: " + String.valueOf(runtime.maxMemory() / 1073741824) + " gb");
    System.out.println("     Free: " + String.valueOf(runtime.freeMemory() / 1073741824) + " gb");
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

  private String getSecDiff(long endTime, long startTime) {
    return String.valueOf((endTime - startTime) / 1000) + "s";
  }

  private Map<String, Integer> getDecimalMap(int maxFamilySize) {
    Map<String, Integer> bdMap = new HashMap<String, Integer>();
    for (int i = 1; i <= maxFamilySize; i++) {
      String[] binaryNumbers = getBinaryStrings(i);
      for (int b = 0; b < binaryNumbers.length; b++) {
        bdMap.put(binaryNumbers[b], b);
      }
    }
    return bdMap;
  }

  private String[] getBinaryStrings(int i) {
    int numberOfValues = (int) Math.pow(2, i);
    String[] biStrings = new String[numberOfValues];
    for (int l = 0; l < numberOfValues; l++) {
      biStrings[l] = String.format("%0" + i + "d", Integer.valueOf(Integer.toBinaryString(l)));
    }

    return biStrings;
  }

  private void updateFamilyCount(float[][] counter, int[] familyLocations, String rowValues) {
    if (rowValues.length() < 10) {
      System.err.println("No Value");
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

  private ArrayList<Family> getFamilies(String file_name, Map<String, Integer> variables) {

    ArrayList<Family> families = new ArrayList<Family>();
    FileInputStream inputStream = null;
    InputStreamReader isr = null;
    try {
      inputStream = new FileInputStream(file_name);
      isr = new InputStreamReader(inputStream, "UTF-8");
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    try (BufferedReader br = new BufferedReader(isr)) {
      String line;
      while ((line = br.readLine()) != null) {
        families.add(new Family(line, variables));
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return families;
  }

  private HashSet<String> hashSetOf(String[] families) {
    HashSet<String> gspFamSet = new HashSet<String>();
    for (int f = 0; f < families.length; f++) {
      gspFamSet.add(families[f]);
    }
    return gspFamSet;
  }

  /**
   * All timers associated with a member will trigger this method when it goes off. The timer will
   * go off if we have not heard from this member in <code> _settings.T_CLEANUP </code> time.
   */
  @Override
  public void handleNotification(Notification notification, Object handback) {
    LocalGossipMember deadMember = (LocalGossipMember) notification.getUserData();
    GossipService.LOGGER.debug("Dead member detected: " + deadMember);
    members.put(deadMember, GossipState.DOWN);
    if (listener != null) {
      listener.gossipEvent(deadMember, GossipState.DOWN);
    }
  }

  public void reviveMember(LocalGossipMember m) {
    for (Entry<LocalGossipMember, GossipState> it : this.members.entrySet()) {
      if (it.getKey().getId().equals(m.getId())) {
        it.getKey().disableTimer();
      }
    }
    members.remove(m);
    members.put(m, GossipState.UP);
    if (listener != null) {
      listener.gossipEvent(m, GossipState.UP);
    }
  }

  public void createOrReviveMember(LocalGossipMember m) {
    members.put(m, GossipState.UP);
    if (listener != null) {
      listener.gossipEvent(m, GossipState.UP);
    }
  }

  public GossipSettings getSettings() {
    return settings;
  }

  // TODO: Use some java 8 goodness for these functions.

  /**
   * @return a read only list of members found in the DOWN state.
   */
  public List<LocalGossipMember> getDeadMembers() {
    List<LocalGossipMember> down = new ArrayList<>();
    for (Entry<LocalGossipMember, GossipState> entry : members.entrySet()) {
      if (GossipState.DOWN.equals(entry.getValue())) {
        down.add(entry.getKey());
      }
    }
    return Collections.unmodifiableList(down);
  }

  /**
   * 
   * @return a read only list of members found in the UP state
   */
  public List<LocalGossipMember> getLiveMembers() {
    List<LocalGossipMember> up = new ArrayList<>();
    for (Entry<LocalGossipMember, GossipState> entry : members.entrySet()) {
      if (GossipState.UP.equals(entry.getValue())) {
        up.add(entry.getKey());
      }
    }
    return Collections.unmodifiableList(up);
  }

  public LocalGossipMember getMyself() {
    return me;
  }

  public List<LocalGossipMember> getDeadList() {
    List<LocalGossipMember> up = new ArrayList<>();
    for (Entry<LocalGossipMember, GossipState> entry : members.entrySet()) {
      if (GossipState.DOWN.equals(entry.getValue())) {
        up.add(entry.getKey());
      }
    }
    return Collections.unmodifiableList(up);
  }

  /**
   * Starts the client. Specifically, start the various cycles for this protocol. Start the gossip
   * thread and start the receiver thread.
   */
  public void init() {
    GossipService.LOGGER.debug("Starting client..");

    for (LocalGossipMember member : members.keySet()) {
      if (member != me) {
        member.startTimeoutTimer();
      }
    }
    GossipManager this_manager = this;

    new Thread(new Runnable() {
      public void run() {
        LOGGER.debug("Node (" + id + ") is preparing gosssip data..");
        boolean prepared = prepareGossipData();
        LOGGER.debug("Node (" + id + ") gosssip data is prepared: " + prepared);
        for (GossipMember startupMember : backupGossipMembersList) {
          if (!startupMember.equals(me)) {
            LocalGossipMember member = new LocalGossipMember(startupMember.getClusterName(),
                startupMember.getUri(), startupMember.getId(), System.currentTimeMillis(),
                this_manager, settings.getCleanupInterval());
            members.put(member, GossipState.UP);
            GossipService.LOGGER.debug(member);
          }
        }
        managerIsReady();

        passiveGossipThread = new OnlyProcessReceivedPassiveGossipThread(this_manager, gossipCore);
        activeGossipThread = new ActiveGossipThread(this_manager, this_manager.gossipCore);
        dataProcessorThread = new DiscDataProcessorThread(this_manager, this_manager.gossipCore);
        
        if (!gossip_all_families) {     
          for (int u = 0; u < uris.length; u++){
            activeGossipThread.addDestPermanentFamilies(uris[u],membersFamilies[u]);
          }
        }
        gossipThreadExecutor.execute(passiveGossipThread);
        activeGossipThread.init(delay_constant);
        dataReaper.init();
        dataProcessorThread.run();
        
        LOGGER.debug("The GossipService is started.");
      }

      private void managerIsReady() {
        GossipService.LOGGER.debug("Node (" + id + ") is setting manager as ready..");
        if (isManagerReady == false) {
          isManagerReady = true;
        } else {
          throw new RuntimeException("Node (" + id + ") manager is already ready!");
        }

      }
    }).start();
  }

  public boolean prepareGossipData() {
    // Set values for every Xij and its E
    // get variables
    variables = new HashMap<String, Integer>();

    FileInputStream inputStream;
    InputStreamReader isr = null;
    try {
      inputStream = new FileInputStream(variablesFilename);
      isr = new InputStreamReader(inputStream, "UTF-8");
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }


    try (BufferedReader br = new BufferedReader(isr)) {
      String line;
      int l = 0;
      while ((line = br.readLine()) != null) {
        variables.put(line, l++);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    // DiSC: initialize node value
    ArrayList<Family> families = getFamilies(String.valueOf(familiesFilename), variables);
    data = new DiscData(families, R);
    data.setFromId(this.id);
    data.setUriFrom(me.getUri().toString());

    // Map <String, Integer> biToDec = getDecimalMap(data.maxFamilySize);
    int numFamilies = families.size();
    Family[] familiesArr = families.toArray(new Family[numFamilies]);

    if (use_stored_vals == false) {
      long startTime = System.currentTimeMillis();
      LOGGER.info(
          "Node (" + id + ") reading input table and counting " + numFamilies + " families..");
      LOGGER.info("Node (" + id + ") block size: " + block_size);

      /**/
      ParallelReader pfr =
          new ParallelReader(tableFilename + '.' + id, num_read_splits, block_size, create_splits);
      pfr.initCounterValues(familiesArr);
      /**/

      /*
       * try (BufferedReader br = new BufferedReader(new FileReader(tableFilename + "." + id))) {
       * String[] row_lines; while ((row_lines = readBlock(br, block_size)).length > 0) { int f = 0;
       * while (f < numFamilies) { Family family = familiesArr[f++]; float[][] family_counter =
       * family.counter; for (int r = 0; r < row_lines.length; r++) {
       * updateFamilyCount(family_counter, family.locations, row_lines[r]); } } } } catch
       * (NumberFormatException e) { e.printStackTrace(); System.exit(0); } catch (IOException e) {
       * e.printStackTrace(); System.exit(0); }
       * 
       */

      long endTime = System.currentTimeMillis();
      LOGGER.info(
          "Node (" + id + ") finished reading and counting in " + getSecDiff(endTime, startTime));
    } else {
      long startTime = System.currentTimeMillis();
      for (int f = 0; f < initCountVals.length; f++) {
        String[] FamilyCounterPair = initCountVals[f].split(":");
        String familyName = FamilyCounterPair[0];
        String[] initCounts = FamilyCounterPair[1].split(",");

        float[][] FC = data.FC.get(familyName);
        int element = 0;
        for (int r = 0; r < 2; r++) {
          for (int c = 0; c < FC[0].length; c++) {
            FC[r][c] = Float.valueOf(initCounts[element++]);
          }
        }
      }
      long endTime = System.currentTimeMillis();
      LOGGER.info("Node (" + id + ") finished reading counts in " + getSecDiff(endTime, startTime));
    }

    LOGGER.info("DiSC: Node (" + id + ") INIT C VALS:" + data.FCtoString());

    if (this.type == AlgorithmType.Sum) {// Store E values
      int f = 0;
      while (f < numFamilies) {
        Family family = familiesArr[f++];
        String family_name = family.familyStr;
        familyGossipCount.put(family_name, 0);
        allGossipCount.put(family_name, 0);
        float[][] fCount = data.FC.get(family_name);
        // set E for fCount[i][j]
        float [][][] E = new float [2][family.counterWidth][];
        for (int row = 0; row < 2; row++) {
          for (int col = 0; col < family.counterWidth; col++) {
            float xVal = fCount[row][col];
            float[] Eij = new float[data.R];
            for (int e = 0; e < data.R; e++) {
              float u = new Random().nextFloat();
              if (xVal == 0)
                Eij[e] = 0;
              else
                Eij[e] = ((-1 * (float) Math.log(1 - u)) / xVal);
            }
            E[row][col] = Eij;
          }
        }
        data.E.put(family_name,E);
      }
    }
    // stop execution after a given time
    // new Terminate(stopTime);
    // LOGGER.debug("DiSC: Node (" + id + ") INIT Es:" + data.EtoString());

    Path filePath = Paths.get("configuration/init.txt");
    try {
      if (!Files.exists(filePath))
        Files.createFile(filePath);
      Files.write(filePath, ("Node_" + id + "_vals=" + data.FCtoSeqString() + "\n").getBytes(),
          StandardOpenOption.APPEND);

    } catch (IOException e) {
      e.printStackTrace();
    }
    return true;
  }

  /**
   * Shutdown the gossip service.
   */
  public void shutdown() {
    gossipServiceRunning.set(false);
    gossipThreadExecutor.shutdown();
    gossipCore.shutdown();
    dataReaper.close();
    if (passiveGossipThread != null) {
      passiveGossipThread.shutdown();
    }
    if (activeGossipThread != null) {
      activeGossipThread.shutdown();
    }
    if (dataProcessorThread != null) {
      dataProcessorThread.shutdown();
    }    

    try {
      boolean result = gossipThreadExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS);
      if (!result) {
        LOGGER.error("executor shutdown timed out");
      }
    } catch (InterruptedException e) {
      LOGGER.error(e);
    }
  }

  public void gossipPerNodeData(GossipDataMessage message) {
    Objects.nonNull(message.getKey());
    Objects.nonNull(message.getTimestamp());
    Objects.nonNull(message.getPayload());
    message.setNodeId(me.getId());
    gossipCore.addPerNodeData(message);
  }

  public void gossipSharedData(SharedGossipDataMessage message) {
    Objects.nonNull(message.getKey());
    Objects.nonNull(message.getTimestamp());
    Objects.nonNull(message.getPayload());
    message.setNodeId(me.getId());
    gossipCore.addSharedData(message);
  }

  public GossipDataMessage findPerNodeGossipData(String nodeId, String key) {
    ConcurrentHashMap<String, GossipDataMessage> j = gossipCore.getPerNodeData().get(nodeId);
    if (j == null) {
      return null;
    } else {
      GossipDataMessage l = j.get(key);
      if (l == null) {
        return null;
      }
      if (l.getExpireAt() != null && l.getExpireAt() < clock.currentTimeMillis()) {
        return null;
      }
      return l;
    }
  }

  public SharedGossipDataMessage findSharedGossipData(String key) {
    SharedGossipDataMessage l = gossipCore.getSharedData().get(key);
    if (l == null) {
      return null;
    }
    if (l.getExpireAt() < clock.currentTimeMillis()) {
      return null;
    } else {
      return l;
    }
  }

  public DataReaper getDataReaper() {
    return dataReaper;
  }

  public DiscData getDiscData() {
    return this.data;
  }

  public void setDiscData(DiscData data) {
    this.data.E = data.E;
    this.data.FC = data.FC;
  }

}
