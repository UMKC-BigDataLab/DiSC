package org.apache.gossip.manager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.gossip.GossipMember;
import org.apache.gossip.LocalGossipMember;
import org.apache.gossip.RemoteGossipMember;
import org.apache.gossip.model.ActiveGossipMessage;
import org.apache.gossip.model.AlgorithmType;
import org.apache.gossip.model.Base;
import org.apache.gossip.model.DiscData;
import org.apache.gossip.model.DiscDataHeader;
import org.apache.gossip.model.DiscGossipMsgType;
import org.apache.gossip.model.Family;
import org.apache.gossip.model.GossipDataMessage;
import org.apache.gossip.model.Response;
import org.apache.gossip.model.SharedGossipDataMessage;
import org.apache.gossip.udp.Trackable;
import org.apache.gossip.udp.UdpActiveGossipMessage;
import org.apache.gossip.udp.UdpActiveGossipOk;
import org.apache.gossip.udp.UdpGossipDataMessage;
import org.apache.gossip.udp.UdpNotAMemberFault;
import org.apache.gossip.udp.UdpSharedGossipDataMessage;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import io.airlift.compress.snappy.SnappyCompressor;

public class GossipCore {



  public static final Logger LOGGER = Logger.getLogger(GossipCore.class);
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final GossipManager gossipManager;
  private ConcurrentHashMap<String, Base> requests;
  private ExecutorService service;
  private final ConcurrentHashMap<String, ConcurrentHashMap<String, GossipDataMessage>> perNodeData;
  private final ConcurrentHashMap<String, SharedGossipDataMessage> sharedData;
  private final BlockingQueue<Runnable> workQueue;
  public ConcurrentLinkedQueue<DiscData> messageQueue;
  private int estimationLogLimit = 400;
  
  private int numTimesEstimated;

  public String getManagerId() {
    return gossipManager.id;
  }

  public GossipCore(GossipManager manager) {
    this.gossipManager = manager;
    requests = new ConcurrentHashMap<>();
    workQueue = new ArrayBlockingQueue<>(1024);
    service = new ThreadPoolExecutor(1, 5, 1, TimeUnit.SECONDS, workQueue, new DiscardPolicy());
    perNodeData = new ConcurrentHashMap<>();
    sharedData = new ConcurrentHashMap<>();
    messageQueue = new ConcurrentLinkedQueue<DiscData>();
    numTimesEstimated = 0;
    estimationLogLimit = manager.logLimit;
    LOGGER.debug("Setting Log Print Limit to "+estimationLogLimit);             

  }

  public void addSharedData(SharedGossipDataMessage message) {
    SharedGossipDataMessage previous = sharedData.get(message.getKey());
    if (previous == null) {
      sharedData.putIfAbsent(message.getKey(), message);
    } else {
      if (previous.getTimestamp() < message.getTimestamp()) {
        sharedData.replace(message.getKey(), previous, message);
      }
    }
  }

  public void addPerNodeData(GossipDataMessage message) {
    ConcurrentHashMap<String, GossipDataMessage> nodeMap = new ConcurrentHashMap<>();
    nodeMap.put(message.getKey(), message);
    nodeMap = perNodeData.putIfAbsent(message.getNodeId(), nodeMap);
    if (nodeMap != null) {
      GossipDataMessage current = nodeMap.get(message.getKey());
      if (current == null) {
        nodeMap.putIfAbsent(message.getKey(), message);
      } else {
        if (current.getTimestamp() < message.getTimestamp()) {
          nodeMap.replace(message.getKey(), current, message);
        }
      }
    }
  }

  public ConcurrentHashMap<String, ConcurrentHashMap<String, GossipDataMessage>> getPerNodeData() {
    return perNodeData;
  }

  public ConcurrentHashMap<String, SharedGossipDataMessage> getSharedData() {
    return sharedData;
  }

  public void shutdown() {
    service.shutdown();
    try {
      service.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      LOGGER.warn(e);
    }
  }

  public void receiveDiscData(DiscData rDD) {

    LOGGER.debug("DiSC: Node (" + gossipManager.id + ") RECEIVED: " + rDD.numFamilies()
        + " Families FROM Node (" + rDD.getFromId() + ") as "+rDD.getType());

    // reply back with the all visible families
    if (rDD.getType() == DiscGossipMsgType.INITIAL){
      try {
        boolean reply = false;
        Long prevTimestamp = gossipManager.lastReceived.get(rDD.getFromId());
        if (prevTimestamp == null || prevTimestamp < rDD.getTimestamp()) {
          gossipManager.lastReceived.put(rDD.getFromId(), rDD.getTimestamp());
          reply = true;
        }

        if (reply) {
          DiscDataHeader hdr = new DiscDataHeader(gossipManager.getMyself(),
              new URI(rDD.getUriFrom()), DiscGossipMsgType.ACK, gossipManager.clockTick);
          hdr.bloomFamilies = rDD.familiesBf;
          
          gossipManager.responseQueue.add(hdr);
          
          LOGGER.debug("DiSC: Node (" + gossipManager.id + ") reply to packet ["
              + rDD.getTimestamp() + "] from (" + rDD.getFromId() + ")");
        }
      } catch (URISyntaxException e) {
        LOGGER.error(
            "DiSC: Node (" + gossipManager.id + ") URI ERROR FOR Node (" + rDD.getFromId() + ")");
        e.printStackTrace();
      }
    }

    // LOGGER.debug("DiSC: Node (" + gossipManager.id + ") RECEIVED: Cs ARE " + rDD.FCtoString() + "
    // FROM Node ("+ rDD.getFromId() + ")");
    // if (gossipManager.type == AlgorithmType.Sum)
    //LOGGER.debug("DiSC: Node (" + gossipManager.id + ") RECEIVED: Es ARE " + rDD.EtoString() + 
    //     "FROM Node ("+ rDD.getFromId() + ")");

    // for every colFam in received data: combine with local
    Set<String> family_names = rDD.E.keySet();
    String[] rcvdFamilies = family_names.toArray(new String[family_names.size()]);

    synchronized (gossipManager.mutex) {  // ************** start synchronized
      for (int fndx = 0; fndx < rcvdFamilies.length; fndx++) {
        boolean isModified = false;
        String family = rcvdFamilies[fndx];
        if (gossipManager.gossip_all_families == false) {
          // has this family been dropped
          if (gossipManager.managerVisibleFamilies.contains(family) == false) {
            gossipManager.managerVisibleFamilies.add(family);
            gossipManager.numberOfVisibleFamilies++;
          }
        }

        boolean isNewFamily = false;
        float[][] lclFC = gossipManager.getDiscData().FC.get(family);
        if (lclFC == null) {
          isNewFamily = true;
          Family newFamily = new Family(family, gossipManager.variables);
          lclFC = newFamily.counter;
        }

        if (lclFC == null) {
          LOGGER.error("DiSC: Node (" + gossipManager.id + ") Family name error: " + family);
        }
        int N = lclFC[0].length;
        float[][][] localE = gossipManager.getDiscData().E.get(family); // E
        float[][][] remoteE = rDD.E.get(family); // E'
        float[][][] newE = new float[2][N][rDD.R]; // newE = min (E,E')

        for (int i = 0; i < 2; i++) {
          for (int j = 0; j < N; j++) {
            if (gossipManager.type == AlgorithmType.Sum) {
              float sumEij = 0;
              for (int r = 0; r < rDD.R; r++) {
                if(remoteE==null){
                  LOGGER.error("DiSC: Node (" + gossipManager.id + ") Received Null E "+family );
                }
                float val = remoteE[i][j][r];
                if (!isNewFamily || localE != null) {
                  // float val = Math.min(localEij[r], remoteEij[r]);
                  //try{

                  /* Feb 28, 2017
                   *
                   * We have decided to choose max(remoteEij, localEij) 
                   * if one of them is 0 to circumvent cases where the 
                   * true count is small but it's widely gossiped as 0.
                   * In the other way choosing the minimum will lead to
                   * high relative error. (e.g. true count 4, estimation 0).
                   */
                  if ( localE[i][j][r] != 0 && remoteE[i][j][r] != 0)
                    val = (localE[i][j][r] < remoteE[i][j][r]) ? localE[i][j][r] : remoteE[i][j][r];
                  else
                    val = (localE[i][j][r] > remoteE[i][j][r]) ? localE[i][j][r] : remoteE[i][j][r];
                  //}
                  //catch (NullPointerException e){
                    //e.printStackTrace(System.out);
                  //}
                }
                newE[i][j][r] = val;
                sumEij += val; // E[0] + E[1] + .. + E[R-1]
              }

              // sum estimate = R / E[0] + E[1] + ... + E[R-1]
              if(sumEij != 0){
                  sumEij = rDD.R / sumEij;
              }
              
              if (lclFC[i][j] != sumEij) {
                isModified = true;
                lclFC[i][j] = sumEij; // Ci,j = R/SumOfE
              }

            } else if (gossipManager.type == AlgorithmType.Ave) {
              float[][] rcvdC = rDD.FC.get(family);
              lclFC[i][j] = ((lclFC[i][j] + rcvdC[i][j]) / 2); // Ci,j = R/SumOfE
            }
          }
        }
        // LOGGER.debug("DiSC: Node (" + gossipManager.id + ") E" + e + " Merge Result:"+
        // Arrays.toString(newE));
        if (isNewFamily) {
          gossipManager.getDiscData().FC.put(family, lclFC);
        }
        gossipManager.getDiscData().E.put(family, newE); // E = newE (that is Ei,j = newEi,j)
        if (isModified && gossipManager.managerPermanentFamilies.contains(family) == false) {
          gossipManager.familyGossipCount.put(family, 0);
        }
      }
      numTimesEstimated++;


    if (numTimesEstimated > estimationLogLimit  || numTimesEstimated == 0) {
        LOGGER.info("DiSC: Node (" + gossipManager.id + ") EST C = "
            + gossipManager.getDiscData().FCtoString());
        numTimesEstimated = 1;
      }
    } // ************** end synchronized
  }

  private boolean dropThisFamily(String familyname) {
    // TODO Auto-generated method stub
    return false;
  }

  public void receive(Base base) {
    if (base instanceof Response) {
      if (base instanceof Trackable) {

        Trackable t = (Trackable) base;
        requests.put(t.getUuid() + "/" + t.getUriFrom(), (Base) t);
      }
    }

    if (base instanceof GossipDataMessage) {
      UdpGossipDataMessage message = (UdpGossipDataMessage) base;
      addPerNodeData(message);

    }
    if (base instanceof SharedGossipDataMessage) {
      UdpSharedGossipDataMessage message = (UdpSharedGossipDataMessage) base;
      addSharedData(message);
    }
    if (base instanceof ActiveGossipMessage) {
      List<GossipMember> remoteGossipMembers = new ArrayList<>();
      RemoteGossipMember senderMember = null;
      UdpActiveGossipMessage activeGossipMessage = (UdpActiveGossipMessage) base;
      for (int i = 0; i < activeGossipMessage.getMembers().size(); i++) {
        URI u = null;
        try {
          u = new URI(activeGossipMessage.getMembers().get(i).getUri());
        } catch (URISyntaxException e) {
          LOGGER.debug("Gossip message with faulty URI", e);
          continue;
        }
        RemoteGossipMember member =
            new RemoteGossipMember(activeGossipMessage.getMembers().get(i).getCluster(), u,
                activeGossipMessage.getMembers().get(i).getId(),
                activeGossipMessage.getMembers().get(i).getHeartbeat());
        if (i == 0) {
          senderMember = member;
        }
        if (!(member.getClusterName().equals(gossipManager.getMyself().getClusterName()))) {
          UdpNotAMemberFault f = new UdpNotAMemberFault();
          f.setException("Not a member of this cluster " + i);
          f.setUriFrom(activeGossipMessage.getUriFrom());
          f.setUuid(activeGossipMessage.getUuid());
          LOGGER.warn(f);
          sendOneWay(f, member.getUri());
          continue;
        }
        remoteGossipMembers.add(member);
      }
      UdpActiveGossipOk o = new UdpActiveGossipOk();
      o.setUriFrom(activeGossipMessage.getUriFrom());
      o.setUuid(activeGossipMessage.getUuid());

      sendOneWay(o, senderMember.getUri());
      mergeLists(gossipManager, senderMember, remoteGossipMembers);
    }
  }

  private void sendInternal(Base message, URI uri) {
    byte[] json_bytes;
    try {
      json_bytes = MAPPER.writeValueAsString(message).getBytes();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    int packet_length = json_bytes.length;
    if (packet_length < GossipManager.MAX_PACKET_SIZE) {
      byte[] buf = UdpUtil.createBuffer(packet_length, json_bytes);
      try (DatagramSocket socket = new DatagramSocket()) {
        InetAddress dest = InetAddress.getByName(uri.getHost());
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, dest, uri.getPort());
        socket.send(datagramPacket);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public Response send(Base message, URI uri) {
    final Trackable t;
    if (message instanceof Trackable) {
      t = (Trackable) message;
    } else {
      t = null;
    }
    sendInternal(message, uri);
    if (t == null) {
      return null;
    }
    final Future<Response> response = service.submit(new Callable<Response>() {
      @Override
      public Response call() throws Exception {
        while (true) {
          Base b = requests.remove(t.getUuid() + "/" + t.getUriFrom());
          if (b != null) {
            return (Response) b;
          }
          try {
            Thread.sleep(0, 1000);
          } catch (InterruptedException e) {

          }
        }
      }
    });

    try {
      return response.get(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      LOGGER.debug(e.getMessage(), e);
      return null;
    } catch (TimeoutException e) {
      boolean cancelled = response.cancel(true);
      LOGGER.debug(String.format("Threadpool timeout attempting to contact %s, cancelled ? %b",
          uri.toString(), cancelled));
      return null;
    } finally {
      if (t != null) {
        requests.remove(t.getUuid() + "/" + t.getUriFrom());
      }
    }

  }

  public void sendToMember(byte[] data, URI u, boolean compress) {
    long startTime = System.currentTimeMillis();
    int decompressedLength = data.length;
    int maxCompressedLength = data.length;
    int compressedLength = data.length;
    byte[] compressed;

    if (compress) {
      SnappyCompressor compressor = new SnappyCompressor();
      maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
      compressed = new byte[maxCompressedLength];
      compressedLength =
          compressor.compress(data, 0, decompressedLength, compressed, 0, maxCompressedLength);
    } else {
      compressed = data;
    }

    long endTime = System.currentTimeMillis();
    long elapsedTime = endTime - startTime;

    LOGGER.debug(
        "%SizeReduction=" + (100 * (1 - ((float) compressedLength / (float) decompressedLength))));
    LOGGER.debug("MsCompressTime=" + elapsedTime);
    LOGGER.debug("PacketSizeBefore=" + decompressedLength);
    LOGGER.debug("PacketSizeAfter=" + compressedLength);
    if (compressedLength < GossipManager.MAX_PACKET_SIZE) {
      try (DatagramSocket socket = new DatagramSocket()) {
        InetAddress dest = InetAddress.getByName(u.getHost());
        DatagramPacket datagramPacket =
            new DatagramPacket(compressed, compressedLength, dest, u.getPort());
        LOGGER.info("SendToMember [" + datagramPacket.getLength() + "] bytes");
        socket.send(datagramPacket);
      } catch (IOException ex) {
        LOGGER.error("sendToMember Error:");
        ex.printStackTrace();
      }
    } else
      LOGGER.error("The length of the to be send message is too large (" + compressedLength + " > "
          + GossipManager.MAX_PACKET_SIZE + ").");

  }

  public void sendOneWay(Base message, URI u) {
    byte[] json_bytes;
    try {
      json_bytes = MAPPER.writeValueAsString(message).getBytes();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    int packet_length = json_bytes.length;
    if (packet_length < GossipManager.MAX_PACKET_SIZE) {
      byte[] buf = UdpUtil.createBuffer(packet_length, json_bytes);
      try (DatagramSocket socket = new DatagramSocket()) {
        InetAddress dest = InetAddress.getByName(u.getHost());
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, dest, u.getPort());
        socket.send(datagramPacket);
      } catch (IOException ex) {
        LOGGER.error("sendOneWay Error:");
        ex.printStackTrace();
      }
    }
  }

  /**
   * Merge remote list (received from peer), and our local member list. Simply, we must update the
   * heartbeats that the remote list has with our list. Also, some additional logic is needed to
   * make sure we have not timed out a member and then immediately received a list with that member.
   * 
   * @param gossipManager
   * @param senderMember
   * @param remoteList
   * 
   *        COPIED FROM PASSIVE GOSSIP THREAD
   */
  protected void mergeLists(GossipManager gossipManager, RemoteGossipMember senderMember,
      List<GossipMember> remoteList) {

    // if the person sending to us is in the dead list consider them up
    for (LocalGossipMember i : gossipManager.getDeadList()) {
      if (i.getId().equals(senderMember.getId())) {
        LOGGER
            .info(gossipManager.getMyself() + " contacted by dead member " + senderMember.getUri());
        LocalGossipMember newLocalMember = new LocalGossipMember(senderMember.getClusterName(),
            senderMember.getUri(), senderMember.getId(), senderMember.getHeartbeat(), gossipManager,
            gossipManager.getSettings().getCleanupInterval());
        gossipManager.reviveMember(newLocalMember);
        newLocalMember.startTimeoutTimer();
      }
    }
    for (GossipMember remoteMember : remoteList) {
      if (remoteMember.getId().equals(gossipManager.getMyself().getId())) {
        continue;
      }
      if (gossipManager.getLiveMembers().contains(remoteMember)) {
        LocalGossipMember localMember = gossipManager.getLiveMembers()
            .get(gossipManager.getLiveMembers().indexOf(remoteMember));
        if (remoteMember.getHeartbeat() > localMember.getHeartbeat()) {
          localMember.setHeartbeat(remoteMember.getHeartbeat());
          localMember.resetTimeoutTimer();
        }
      } else if (!gossipManager.getLiveMembers().contains(remoteMember)
          && !gossipManager.getDeadList().contains(remoteMember)) {
        LocalGossipMember newLocalMember = new LocalGossipMember(remoteMember.getClusterName(),
            remoteMember.getUri(), remoteMember.getId(), remoteMember.getHeartbeat(), gossipManager,
            gossipManager.getSettings().getCleanupInterval());
        gossipManager.createOrReviveMember(newLocalMember);
        newLocalMember.startTimeoutTimer();
      } else {
        if (gossipManager.getDeadList().contains(remoteMember)) {
          LocalGossipMember localDeadMember =
              gossipManager.getDeadList().get(gossipManager.getDeadList().indexOf(remoteMember));
          if (remoteMember.getHeartbeat() > localDeadMember.getHeartbeat()) {
            LocalGossipMember newLocalMember = new LocalGossipMember(remoteMember.getClusterName(),
                remoteMember.getUri(), remoteMember.getId(), remoteMember.getHeartbeat(),
                gossipManager, gossipManager.getSettings().getCleanupInterval());
            gossipManager.reviveMember(newLocalMember);
            newLocalMember.startTimeoutTimer();
            LOGGER.debug("Removed remote member " + remoteMember.getAddress()
                + " from dead list and added to local member list.");
          } else {
            LOGGER.debug("me " + gossipManager.getMyself());
            LOGGER.debug("sender " + senderMember);
            LOGGER.debug("remote " + remoteList);
            LOGGER.debug("live " + gossipManager.getLiveMembers());
            LOGGER.debug("dead " + gossipManager.getDeadList());
          }
        } else {
          LOGGER.debug("me " + gossipManager.getMyself());
          LOGGER.debug("sender " + senderMember);
          LOGGER.debug("remote " + remoteList);
          LOGGER.debug("live " + gossipManager.getLiveMembers());
          LOGGER.debug("dead " + gossipManager.getDeadList());
          // throw new IllegalArgumentException("wtf");
        }
      }
    }
  }
}
