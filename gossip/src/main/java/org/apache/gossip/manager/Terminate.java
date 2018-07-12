package org.apache.gossip.manager;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

class Terminate {

	Timer timer;
	public static final Logger LOGGER = Logger.getLogger(GossipManager.class);
	public Terminate(int seconds) {
		timer = new Timer();
		timer.schedule(new RemindTask(), seconds * 1000);
	}
	class RemindTask extends TimerTask {
		public void run() {
			LOGGER.info("Node exiting..");
			timer.cancel();
			System.exit(0);
		}
	}
}