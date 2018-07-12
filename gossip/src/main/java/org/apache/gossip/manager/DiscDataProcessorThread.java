package org.apache.gossip.manager;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.gossip.model.DiscData;
import org.apache.gossip.model.DiscDataHeader;
import org.apache.log4j.Logger;

public class DiscDataProcessorThread implements Runnable {
  private static final Logger LOGGER = Logger.getLogger(DiscDataProcessorThread.class);
  private final GossipManager gossipManager;
  private final GossipCore gossipCore;
  private final AtomicBoolean keepRunning;
  private String previousResponseId;


  public DiscDataProcessorThread(GossipManager manager, GossipCore gCore) {
    gossipManager = manager;
    gossipCore = gCore;
    keepRunning = new AtomicBoolean(true);
    previousResponseId = "";
  }
  
  @Override
  public void run() {
    while (keepRunning.get()) {
      
      // send all pending responses
      DiscDataHeader hdr = gossipManager.responseQueue.poll();
      while (hdr != null) { // send all responses
        gossipManager.activeGossipThread.sendDiscData(hdr);
        hdr = gossipManager.responseQueue.poll();
      }
      
      // process an incoming message
      DiscData data = gossipCore.messageQueue.poll();
      if (data != null){
        gossipCore.receiveDiscData(data);
        
        // if the incoming message is a new response (not from the previous sender):
        //   the clock can tick.
        String currentResponseId = data.getFromId();
        if (currentResponseId.equals(previousResponseId) == false){
          gossipManager.activeGossipThread.canTick.set(true);
          previousResponseId = currentResponseId;
        }
      }
      
    }
  }

  public void shutdown() {
    keepRunning.set(false);
  }

}
