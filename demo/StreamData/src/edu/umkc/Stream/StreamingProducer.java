package edu.umkc.Stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umkc.Constants.DiSCConstants;
import edu.umkc.Util.PropertyReader;

@ServerEndpoint("/discStream")
public class StreamingProducer extends TailerListenerAdapter {
	
	static {
		System.setProperty("log4j.configurationFile", "log4j2.properties");
	}

	private static Set<Session> allSessions;
	private static boolean isInitialized = false;
	private static BlockingQueue<String> queue;
	private static final Logger logger = LogManager.getLogger(StreamingProducer.class.getName());
	
	private void init() {
		if(!isInitialized) {
			isInitialized = true;
			queue = new ArrayBlockingQueue<String>(2000000000);
			StreamingConsumer consumer = new StreamingConsumer(queue, allSessions, System.currentTimeMillis());
		    new Thread(consumer).start();
	    }
	}

	@OnOpen
	public void streamData(Session session) {
		logger.debug("StreamingProducer :: streamData ::  Start");
		allSessions = session.getOpenSessions();
		init();
		if (allSessions != null && allSessions.size() != 0) {
			try(BufferedReader br = new BufferedReader(new FileReader(new File(PropertyReader.getInstance().getProperty(DiSCConstants.LOG_FILE))))) {
				while(true) {
					//Making the thread sleep in order to effectively accept the huge line.
					Thread.sleep(1000);
					String line = null;
					while((line = br.readLine()) != null) {
						queue.put(line);
					}
				}
			} catch(Exception e) {
				logger.error("StreamingProducer :: streamData ::  Exception :: " + e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public void handle(String line) {
		try {
			queue.put(line);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}