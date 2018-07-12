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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.gossip.LocalGossipMember;
import org.apache.gossip.model.ActiveGossipOk;
import org.apache.gossip.model.DiscData;
import org.apache.gossip.model.DiscDataHeader;
import org.apache.gossip.model.DiscGossipMsgType;
import org.apache.gossip.model.GossipDataMessage;
import org.apache.gossip.model.GossipMember;
import org.apache.gossip.model.Response;
import org.apache.gossip.model.SharedGossipDataMessage;
import org.apache.gossip.udp.UdpActiveGossipMessage;
import org.apache.gossip.udp.UdpGossipDataMessage;
import org.apache.gossip.udp.UdpSharedGossipDataMessage;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

/**
 * [The active thread: periodically send gossip request.] The class handles gossiping the membership
 * list. This information is important to maintaining a common state among all the nodes, and is
 * important for detecting failures.
 */
public class ActiveGossipThread {

  private static final Logger LOGGER = Logger.getLogger(ActiveGossipThread.class);
  private final double PACKING_FACTOR; //syn 1, higgs 3, twtr 2 
  private final GossipManager gossipManager;
  private final Random random;
  private final GossipCore gossipCore;
  private ScheduledExecutorService scheduledExecutorService;
  private ObjectMapper MAPPER = new ObjectMapper();
  public AtomicBoolean canTick;
  private String lastGossipedDest;
  private HashMap<String, Set> destMmbrPermanentFamilies;
  private int dropCount, gossipNoneZeroCount, gossipZeroCount, totalCount, p1, p2;

  public ActiveGossipThread(GossipManager gossipManager, GossipCore gossipCore) {
    this.gossipManager = gossipManager;
    random = new Random();
    this.gossipCore = gossipCore;
    this.scheduledExecutorService = Executors.newScheduledThreadPool(2);
    canTick = new AtomicBoolean(false);
    lastGossipedDest = "";
    destMmbrPermanentFamilies = new HashMap<String, Set>();
    p1 = gossipManager.P1;
    p2 = gossipManager.P2;
    PACKING_FACTOR = gossipManager.AGT_PACKING_FACTOR;
    LOGGER.debug("Drop Probabilities: " + p1 +"  "+p2);                     

  }

  public void addDestPermanentFamilies(String dst, String pfams) {
    destMmbrPermanentFamilies.put(dst, new HashSet<String>(Arrays.asList(pfams.split(";"))));
  }

  public void init(double delayConstant) {
    scheduledExecutorService.scheduleAtFixedRate(
        () -> sendMembershipList(gossipManager.getMyself(), gossipManager.getLiveMembers()), 0,
        gossipManager.getSettings().getGossipInterval(), TimeUnit.MILLISECONDS);
    scheduledExecutorService.scheduleAtFixedRate(
        () -> sendMembershipList(gossipManager.getMyself(), gossipManager.getDeadMembers()), 0,
        gossipManager.getSettings().getGossipInterval(), TimeUnit.MILLISECONDS);
    scheduledExecutorService.scheduleAtFixedRate(
        () -> sendPerNodeData(gossipManager.getMyself(), gossipManager.getLiveMembers()), 0,
        gossipManager.getSettings().getGossipInterval(), TimeUnit.MILLISECONDS);
    scheduledExecutorService.scheduleAtFixedRate(
        () -> sendSharedData(gossipManager.getMyself(), gossipManager.getLiveMembers()), 0,
        gossipManager.getSettings().getGossipInterval(), TimeUnit.MILLISECONDS);
    scheduledExecutorService.schedule(() -> scheduleSendDiscData(gossipManager.getMyself(),
        gossipManager.getLiveMembers(), delayConstant), 0, TimeUnit.MILLISECONDS);
  }

  private void scheduleSendDiscData(LocalGossipMember member, List<LocalGossipMember> liveMembers,
      double constant) {
    Runnable run = new Runnable() {
      public void run() {
        try {
          LOGGER.debug("DiSC: Node (" + member.getId() + ") manager is ready? "
              + gossipManager.isManagerReady);
          if (gossipManager.isManagerReady) {
            LOGGER.debug("DiSC: Node (" + member.getId() + ") Gossiping...");
            LocalGossipMember partner = selectPartner(liveMembers);
            if (member == null) {
              LOGGER.debug("Send sendDiscData() is called without action");
              return;
            }

            // gossip new data
            DiscDataHeader hdr = new DiscDataHeader(member, partner.getUri(),
                DiscGossipMsgType.INITIAL, gossipManager.clockTick);
            sendDiscData(hdr);

            while (canTick.get() == false) {
              // wait for response
            }
            gossipManager.clockTick++;
            canTick.set(false);
          }
        } finally {
          double x = random.nextDouble();
          double lambda = 1;

          double delay = constant * (-1) * Math.log(1 - x) / lambda;
          LOGGER.info("DiSC: Node (" + member.getId() + ") DELAY:" + String.valueOf(delay));
          scheduledExecutorService.schedule(this, (long) delay, TimeUnit.SECONDS);
        }
      }
    };
    run.run();
  }

  public void shutdown() {
    scheduledExecutorService.shutdown();
    try {
      scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      LOGGER.debug("Issue during shurdown" + e);
    }
  }

  public void sendSharedData(LocalGossipMember me, List<LocalGossipMember> memberList) {
    LocalGossipMember member = selectPartner(memberList);
    if (member == null) {
      LOGGER.debug("Send sendMembershipList() is called without action");
      return;
    }
    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setSoTimeout(gossipManager.getSettings().getGossipInterval());
      for (Entry<String, SharedGossipDataMessage> innerEntry : this.gossipCore.getSharedData()
          .entrySet()) {
        UdpSharedGossipDataMessage message = new UdpSharedGossipDataMessage();
        message.setUuid(UUID.randomUUID().toString());
        message.setUriFrom(me.getId());
        message.setExpireAt(innerEntry.getValue().getExpireAt());
        message.setKey(innerEntry.getValue().getKey());
        message.setNodeId(innerEntry.getValue().getNodeId());
        message.setTimestamp(innerEntry.getValue().getTimestamp());
        message.setPayload(innerEntry.getValue().getPayload());
        message.setTimestamp(innerEntry.getValue().getTimestamp());
        byte[] json_bytes = MAPPER.writeValueAsString(message).getBytes();
        int packet_length = json_bytes.length;
        if (packet_length < GossipManager.MAX_PACKET_SIZE) {
          gossipCore.sendOneWay(message, member.getUri());
        } else {
          LOGGER.error("The length of the to be send message is too large (" + packet_length + " > "
              + GossipManager.MAX_PACKET_SIZE + ").");
        }
      }
    } catch (IOException e1) {
      LOGGER.warn(e1);
    }
  }

  public void sendPerNodeData(LocalGossipMember me, List<LocalGossipMember> memberList) {
    LocalGossipMember member = selectPartner(memberList);
    if (member == null) {
      LOGGER.debug("Send sendMembershipList() is called without action");
      return;
    }
    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setSoTimeout(gossipManager.getSettings().getGossipInterval());
      for (Entry<String, ConcurrentHashMap<String, GossipDataMessage>> entry : gossipCore
          .getPerNodeData().entrySet()) {
        for (Entry<String, GossipDataMessage> innerEntry : entry.getValue().entrySet()) {
          UdpGossipDataMessage message = new UdpGossipDataMessage();
          message.setUuid(UUID.randomUUID().toString());
          message.setUriFrom(me.getId());
          message.setExpireAt(innerEntry.getValue().getExpireAt());
          message.setKey(innerEntry.getValue().getKey());
          message.setNodeId(innerEntry.getValue().getNodeId());
          message.setTimestamp(innerEntry.getValue().getTimestamp());
          message.setPayload(innerEntry.getValue().getPayload());
          message.setTimestamp(innerEntry.getValue().getTimestamp());
          byte[] json_bytes = MAPPER.writeValueAsString(message).getBytes();
          int packet_length = json_bytes.length;
          if (packet_length < GossipManager.MAX_PACKET_SIZE) {
            gossipCore.sendOneWay(message, member.getUri());
          } else {
            LOGGER.error("The length of the to be send message is too large (" + packet_length
                + " > " + GossipManager.MAX_PACKET_SIZE + ").");
          }
        }
      }
    } catch (IOException e1) {
      LOGGER.warn(e1);
    }
  }

  public synchronized void sendDiscData(DiscDataHeader ddh) {
    LocalGossipMember me = ddh.mmbr;
    URI mmbr = ddh.uri;
    DiscGossipMsgType type = ddh.type;
    long timestamp = ddh.clockTick;
    BloomFilter rcvdBloomFams = ddh.bloomFamilies;

    LOGGER.debug("Node (" + me.getId() + ") " + type + " gossiping with " + mmbr);
    DatagramSocket socket = null;
    try {
      socket = new DatagramSocket();
      socket.setSoTimeout(gossipManager.getSettings().getGossipInterval());
    } catch (SocketException e1) {
      e1.printStackTrace();
      return;
    }

    Iterator<String> family = null;
    Iterator<String> AckFamilies = null;
    HashMap<String, float[][][]> localCopyE = new HashMap<String, float[][][]>();
    HashSet<String> bloomFamilies = null;

    int R;
    synchronized (gossipManager.mutex) {
      DiscData local = gossipManager.getDiscData();
      Set<String> families;
      if (gossipManager.gossip_all_families) {
        families = local.FC.keySet(); // visibleFamilies can't be used (it's null)
      } else {
        // get visibleFamilies
        families = gossipManager.managerVisibleFamilies;
      }
      family = families.iterator();



      boolean sameDestMmbr = false;
      String destMmbr = mmbr.toString();

      Set destPermanentFamilies = null;
      if (destMmbr.equals(lastGossipedDest)) {
        sameDestMmbr = true;
        destPermanentFamilies = destMmbrPermanentFamilies.get(destMmbr);
      }

      if (sameDestMmbr)
        LOGGER.debug("DiSC: Node (" + me.getId() + " ) SameDest=True");
      else
        LOGGER.debug("DiSC: Node (" + me.getId() + " ) SameDest=False");

      bloomFamilies = new HashSet<String>();
      if (gossipManager.gossip_all_families == false) {
        // if dropping is allowed (i.e. not an ACK message), drop some families.
        if (type != DiscGossipMsgType.ACK) {
          LinkedList<String> visibleFamiliesCopy = new LinkedList<String>();
          // for every visible family
          int num_dropped = 0;
          int num_all = 0;
          dropCount = gossipNoneZeroCount = gossipZeroCount = totalCount = 0;

          while (family.hasNext()) {
            num_all++;
            String familyname = family.next();
            totalCount++;

            // if drop
            if (gossipManager.managerPermanentFamilies.contains(familyname) == false
                && dropThisFamily(familyname, sameDestMmbr, destPermanentFamilies, p1, p2)) {
              // drop the families (rest E rand vars and make invisible):

              // remove family from visible families
              family.remove();
              num_dropped++;

              // remove stored E random variables for this family

              // int cw = counterWidth(familyname);
              // resetFamilyE(local.E.get(familyname),cw);

              local.FC.remove(familyname);
              local.E.remove(familyname);

              gossipManager.numberOfVisibleFamilies--;
            } // end if drop
            // if do not drop the family, gossip it
            else {
              visibleFamiliesCopy.add(familyname);
              float[][][] e = local.E.get(familyname);
              // LOGGER.debug(familyname + " at Node (" + me.getId() + ") Local E is null? " + (e ==
              // null));
              float[][][] ec = e.clone();
              // LOGGER.debug(familyname + " at Node (" + me.getId() + ") Local E Clone is null? " +
              // (ec == null));
              localCopyE.put(familyname, ec);
              // family gossip count ++
              gossipManager.familyGossipCount.put(familyname,
                  gossipManager.familyGossipCount.get(familyname) + 1);
              gossipManager.allGossipCount.put(familyname,
                  gossipManager.allGossipCount.get(familyname) + 1);

              bloomFamilies.add(familyname);

            }
          }

          // dropCount = gossipNoneZeroCount = gossipZeroCount = totalCount
          LOGGER.debug(
              "Node (" + me.getId() + ") dropping " + num_dropped + "/" + num_all + " families");
          LOGGER.debug("Node (" + me.getId() + ") dropping consideration stats { " + " Total:"
              + totalCount + ", gossipZero:" + gossipZeroCount + ", gossipNotZero:"
              + gossipNoneZeroCount + ", drop:" + dropCount + "}");

          // use non-dropped visibleFamilies
          // replaced with bloom families /family = visibleFamiliesCopy.iterator();


        } // end if (dropFamilies) != ACK
        else { // is an ACK
          Iterator<String> rbf = families.iterator();
          while (rbf.hasNext()) {
            String f = rbf.next();
            if (rcvdBloomFams.mightContain(f)) {
              float[][][] E = clone(local.E.get(f));
              if (E != null) {
                localCopyE.put(f, E);
                bloomFamilies.add(f);
              }
            }
          }

          // localCopyE.putAll(local.E);
        }
        LOGGER.info("Node (" + me.getId() + ") has [" + gossipManager.numberOfVisibleFamilies
            + "] visible families");
      }

      R = local.R;

    }


    lastGossipedDest = mmbr.toString();
    DiscData packData = new DiscData();
    packData.setFromId(me.getId());
    packData.setUriFrom(me.getUri().toString());
    packData.R = R;
    packData.setType(type);
    packData.setTimestamp(timestamp);
    // if init, not needed for ack
    packData.setBloomFamilies(asBloomFilter(bloomFamilies));


    // packData.FC = new HashMap<String, float[][]>();
    packData.E = new HashMap<String, float[][][]>();

    int sizeOfOne;
    int i = 0;
    int id = new Random().nextInt(1000);
    family = bloomFamilies.iterator();
    while (family.hasNext()) {
      i++;
      String f = family.next();

      // packData.FC.put(f, local.FC.get(f));
      float[][][] Ef = localCopyE.get(f);
      // LOGGER.debug("Node (" + me.getId() + ") Pack E is null? " + (Ef == null));
      packData.E.put(f, Ef);


      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ObjectOutputStream os = null;
      try {
        os = new ObjectOutputStream(outputStream);
        os.writeObject(packData);
      } catch (IOException e) {
        e.printStackTrace();
      }

      byte[] data = outputStream.toByteArray();
      int packet_length = data.length;
      sizeOfOne = packet_length / i;
      float estSize = (float) packet_length + sizeOfOne;

      // if (gossipManager.compressDiscData)
      // estSize = packet_length - (int) (packet_length * 0.6f) + sizeOfOne;

      if (estSize >= GossipManager.MAX_PACKET_SIZE * PACKING_FACTOR || family.hasNext() == false) {
        // LOGGER.debug(
        // "Node (" + me.getId() + ") Sending: P(" + id + ") One = " + sizeOfOne + " x " + i);
        i = 0;
        LOGGER.info("DiSC: Node (" + me.getId() + ") DiSC Data Sending [" + packet_length
            + "] bytes to " + mmbr + " as " + type);
        gossipCore.sendToMember(data, mmbr, gossipManager.compressDiscData);
        packData.FC = new HashMap<String, float[][]>();
        packData.E = new HashMap<String, float[][][]>();
        try {
          os.close();
          outputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }

      }
    }
  }

  private BloomFilter asBloomFilter(HashSet<String> bloomFamilies) {
    BloomFilter<String> bf = BloomFilter.create(Funnels.stringFunnel(Charset.forName("UTF-8")),
        bloomFamilies.size(), 0.05);
    Iterator<String> bfItr = bloomFamilies.iterator();
    while (bfItr.hasNext()) {
      bf.put(bfItr.next());
    }
    return bf;

  }

  private float[][][] clone(float[][][] origin) {
    if (origin == null) {
      return null;
    }

    final int ROWS = origin.length;
    final int COLS = origin[0].length;
    final int SLOTS = origin[0][0].length;

    float[][][] cloned = new float[ROWS][COLS][SLOTS];
    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        for (int z = 0; z < SLOTS; z++) {
          cloned[r][c][z] = origin[r][c][z];
        }
      }
    }
    return cloned;
  }

  private int counterWidth(String familyname) {
    int num_members = countOccurrences(familyname, ',');
    return (int) Math.pow(2, num_members);
  }

  public int countOccurrences(String string, char ch) {
    int count = 0;
    int len = string.length();
    for (int i = 0; i < len; i++) {
      if (string.charAt(i) == ch) {
        count++;
      }
    }
    return count;
  }

  private void resetFamilyE(float[][][] E, int width) {
    int R = E[0][0].length;
    for (int r = 0; r < 2; r++) {
      for (int c = 0; c < width; c++) {
        for (int e = 0; e < R; e++) {
          E[r][c][e] = Float.POSITIVE_INFINITY;
        }
      }
    }
  }

  private boolean dropThisFamily(String familyname, boolean sameDestMmbr,
      Set destPermanentFamilies, int p1, int p2) {

    // has this family been gossiped? if no, don't drop
    if (gossipManager.familyGossipCount.get(familyname) == 0) {
      gossipZeroCount++;
      return false;
    }

    // if yes, flip a biased coin
    Random coin = new Random();
    int result = coin.nextInt(100);

    // if same node as in the previous round, and the family is permanent on that node,
    // drop with p = 0.8
    if (sameDestMmbr && destPermanentFamilies.contains(familyname)) {
      if (result < p1) {
        dropCount++;
        return true;
      }
    }
    // else drop with p = 0.4
    else if (result < p2) {
      dropCount++;
      return true;
    }

    gossipNoneZeroCount++;
    return false;

  }

  /**
   * Performs the sending of the membership list, after we have incremented our own heartbeat.
   */
  protected void sendMembershipList(LocalGossipMember me, List<LocalGossipMember> memberList) {
    me.setHeartbeat(System.currentTimeMillis());
    LocalGossipMember member = selectPartner(memberList);
    if (member == null) {
      LOGGER.debug("Send sendMembershipList() is called without action");
      return;
    } else {
      LOGGER.debug("Send sendMembershipList() is called to " + member.toString());
    }

    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setSoTimeout(gossipManager.getSettings().getGossipInterval());
      UdpActiveGossipMessage message = new UdpActiveGossipMessage();
      message.setUriFrom(gossipManager.getMyself().getUri().toASCIIString());
      message.setUuid(UUID.randomUUID().toString());
      message.getMembers().add(convert(me));
      for (LocalGossipMember other : memberList) {
        message.getMembers().add(convert(other));
      }
      byte[] json_bytes = MAPPER.writeValueAsString(message).getBytes();
      int packet_length = json_bytes.length;
      if (packet_length < GossipManager.MAX_PACKET_SIZE) {
        Response r = gossipCore.send(message, member.getUri());
        if (r instanceof ActiveGossipOk) {
          // maybe count metrics here
        } else {
          LOGGER.warn("Message " + message + " generated response " + r);
        }
      } else {
        LOGGER.error("The length of the to be send message is too large (" + packet_length + " > "
            + GossipManager.MAX_PACKET_SIZE + ").");
      }
    } catch (IOException e1) {
      LOGGER.warn(e1);
    }
  }

  /**
   * 
   * @param memberList The list of members which are stored in the local list of members.
   * @return The chosen LocalGossipMember to gossip with.
   */
  protected LocalGossipMember selectPartner(List<LocalGossipMember> memberList) {
    LocalGossipMember member = null;
    if (memberList.size() > 0) {
      int randomNeighborIndex = random.nextInt(memberList.size());
      member = memberList.get(randomNeighborIndex);
    } else {
      LOGGER.debug("I am alone in this world.");
    }
    return member;
  }

  private GossipMember convert(LocalGossipMember member) {
    GossipMember gm = new GossipMember();
    gm.setCluster(member.getClusterName());
    gm.setHeartbeat(member.getHeartbeat());
    gm.setUri(member.getUri().toASCIIString());
    gm.setId(member.getId());
    return gm;
  }
}
