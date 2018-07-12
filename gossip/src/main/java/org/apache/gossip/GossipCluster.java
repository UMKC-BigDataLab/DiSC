package org.apache.gossip;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

public class GossipCluster {
  private static final String clusterName = "Cluster DiSC";
  private static final Logger LOGGER = Logger.getLogger(GossipCluster.class);

  public static void main(String[] args)
      throws URISyntaxException, UnknownHostException, InterruptedException {
    // Logger.getRootLogger().setLevel(Level.OFF);

    // create a file to store initial values
    Path filePath = Paths.get("configuration/init.txt");
    try {
      if (!Files.exists(filePath))
        Files.createFile(filePath);
      Files.write(filePath, "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // load configuration file
    FileInputStream inputStream;
    Properties prpts = null;
    try {
      inputStream = new FileInputStream("configuration/config.txt");
      prpts = new Properties();
      prpts.load(inputStream);
      inputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }


    boolean readMList = Boolean.parseBoolean(prpts.get("ReadMembershipList").toString()); 
    int numLocalMembers = Integer.parseInt(prpts.get("LocalMmbrCount").toString());
    
    // To gossip you need one or more seed nodes. Seed is just a list of places to initially connect
    // to.
    LOGGER.debug("Seeding..");
    LOGGER.debug("Starting gossip..");
    String[] ips = prpts.get("IPS").toString().split(";");

    String my_ip = getMyIp();
    int id = getMyId(my_ip, ips);
    int node = 0;
    int port = 0;

    try {
      id = Integer.parseInt(prpts.get("UseID").toString());
      port = id * 10;
    } catch (Exception e) {
      LOGGER.debug("No ID in config");
    }

    List<GossipService> peers = new ArrayList<>();
    
    for (node = 0; node < numLocalMembers + 1; node++) {
        
      String node_id = id + "-" + node;
      LOGGER.debug("Setting up Node ("+ node_id+ ")..");
      
      List<GossipMember> MmbrList = new ArrayList<>();
      if (readMList) { // read list from file
        String members = (String) prpts.get("Node_" + node_id + "_mmbrs");
        MmbrList = parseMembers(members);
      }
      URI peerUri = new URI("udp://" + my_ip + ":" + (20000 + port));
      // record the info for future reference
      writeMemList(node_id, MmbrList, filePath);

      GossipService gossipService = new GossipService(clusterName, peerUri, node_id, MmbrList, new GossipSettings(), null);
      peers.add(gossipService);
      LOGGER.debug("Node (" + node_id + ") gossip service starting..");
      gossipService.start();
      port++;
    }

    int stopTimeSeconds = 1000 * Integer.parseInt(prpts.get("StopTime").toString());;
    Timer timer = new Timer();
    timer.schedule(new TerminateTask(timer, peers, stopTimeSeconds), stopTimeSeconds);
  }


  static class TerminateTask extends TimerTask {
    private final List<GossipService> peers_list;
    private int seconds;
    private Timer timer;

    public TerminateTask(Timer timer, List<GossipService> peers, int seconds) {
      peers_list = peers;
      this.seconds = seconds;
      this.timer = timer;
    }

    @Override
    public void run() {
      //timer.cancel();
      boolean oneIsPreparing = false;
      int numPeers = peers_list.size();
      for (int p = 0; p < numPeers; p++) {
        if (seconds > 1 && peers_list.get(p).isManagerReady() == false) {
          oneIsPreparing = true;
          break;
        }
      }
      if (oneIsPreparing) {
        //seconds = seconds/2;
        timer.schedule(new TerminateTask(timer, peers_list, seconds), seconds);
      } else {
        System.exit(0);
      }
    }
  }

  private static void writeMemList(String node_id, List<GossipMember> globalMmbrList,
      Path filePath) {
    try {
      Files.write(filePath,
          ("Node_" + node_id + "_mmbrs=" + StringOf(globalMmbrList) + "\n").getBytes(),
          StandardOpenOption.APPEND);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private static int getMyId(String my_ip, String[] ips) {
    int id = 1;
    for (String ip : ips) {
      if (my_ip.equals(ip))
        return id;
      id++;
    }
    LOGGER.error("IP Address Not Found: " + my_ip + " " + Arrays.deepToString(ips));
    return -1;
  }

  private static String getMyIp() {
    try {
      InetAddress ipAddr = InetAddress.getLocalHost();
      return ipAddr.getHostAddress();
    } catch (UnknownHostException ex) {
      ex.printStackTrace();
    }
    return "IpAddressProblem";
  }

  private static ArrayList<GossipMember> parseMembers(String members) throws URISyntaxException {
    if (members == null){
      LOGGER.error("Members Error: IP address possible mismatch. Check the configuration file: "
          + System.getProperty("user.dir") + "/configuration/config.txt");
  
      throw new NullPointerException();
    }
    ArrayList<GossipMember> list = new ArrayList<>();
    
    String[] mInfo = members.split(";"); // 127.0.0.1:20002,2;127.0.0.1:20001,1;
    for (String i : mInfo) {
      String[] element = i.split(",");
      URI uri = new URI("udp://" + element[0]);
      String id = element[1];
      list.add(new RemoteGossipMember(clusterName, uri, id));
    }
    return list;
  }

  private static String StringOf(List<GossipMember> startupMembers) {
    StringBuilder str = new StringBuilder();
    for (GossipMember member : startupMembers) {
      str.append(member.getAddress() + "," + member.getId() + ";");
    }
    return str.toString();
  }

}
