package edu.umkc.Stream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import javax.websocket.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umkc.Compute.ErrorCalculator;
import edu.umkc.Constants.DiSCConstants;
import edu.umkc.Util.CommonUtil;
import edu.umkc.Util.PropertyReader;

public class StreamingConsumer implements Runnable {

	protected BlockingQueue<String> queue;
	private Set<Session> allSessions;
	private long startTime;
	private static final Logger logger = LogManager.getLogger(StreamingConsumer.class.getName());

	public StreamingConsumer(BlockingQueue<String> queue, Set<Session> allSessions, long startTime) {
		this.queue = queue;
		this.allSessions = allSessions;
		this.startTime = startTime;
	}

	@Override
	public void run() {
		while(true) {
			try {
				String line = queue.take();
				for (Session session : allSessions) {
					if (line.contains(CommonUtil.getDelimter())) { //Streaming Estimated Counts
						String[] arr = line.split("=");
						if (arr != null) {
							try {
								try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(DiSCConstants.EST_C_FILE)))) {
									bw.write(arr[1].trim());
								} catch(Exception e) {
									logger.error("StreamingConsumer :: run ::  Exception encountered while writing the estimated counts :: " + e);
									e.printStackTrace();
								}
								Map<String, LinkedList<LinkedList<Double>>> estimatedCounts = CommonUtil.convertJsonToMap(arr[1].trim());
								Map<String, LinkedList<LinkedList<Double>>> trueCounts = CommonUtil.convertJsonToMap(CommonUtil.getTrueCount());
								String result = ErrorCalculator.calculateError(trueCounts, estimatedCounts, PropertyReader.getInstance().getProperty(DiSCConstants.FAMILY), startTime);
								if (result != null) {
									logger.debug("StreamingConsumer :: run :: estimated count result :: " + result);
									try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(DiSCConstants.EST_C_SUM_FILE), true))) {
										bw.write(result + "\n");
									} catch(Exception e) {
										logger.error("StreamingConsumer :: run ::  Exception encountered while writing the estimated counts :: " + e);
										e.printStackTrace();
									}
									session.getBasicRemote().sendText("EstC::"+result);
								} else {
									logger.error("StreamingConsumer :: run ::  Result was null");
								}
							} catch(Exception e) {
								logger.error("StreamingConsumer :: run ::  Exception encountered while calculating error :: " + e);
								e.printStackTrace();
							}
						}
					} else if (line.contains(DiSCConstants.FAMILY_SIZE_SEARCH_STRING)) { //Streaming Family Size
						String[] arr = line.trim().split(" ");
						String node = arr[3].trim().replaceAll("\\(", "").replaceAll("\\)","");
						String familySize = arr[5].trim().replaceAll("\\[", "").replaceAll("\\]","");
						String result = CommonUtil.getNode(node) + "::" + familySize;
						logger.debug("StreamingConsumer :: run :: fmaiy size result  " + result);
						session.getBasicRemote().sendText(result);
					} else if(line.contains(DiSCConstants.NODE_SEARCH_STRING)) { //Streaming Node Communication
						String[] arr = line.split("::");
						String toNode = arr[6].trim();
						String result = "Node::" + CommonUtil.getNodeNum(toNode);
						logger.debug("StreamingConsumer :: run :: to node result :: " + result);
						session.getBasicRemote().sendText(result);
					}
				}
			} catch (Exception e) {
				logger.error("StreamingConsumer :: run ::  Exception encountered :: " + e);
				e.printStackTrace();

			}
		}
	}

}
