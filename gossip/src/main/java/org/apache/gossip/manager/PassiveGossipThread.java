/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.gossip.manager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.gossip.model.Base;
import org.apache.gossip.model.DiscData;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import io.airlift.compress.snappy.SnappyDecompressor;

/**
 * [The passive thread: reply to incoming gossip request.] This class handles
 * the passive cycle, where this client has received an incoming message. For
 * now, this message is always the membership list, but if you choose to gossip
 * additional information, you will need some logic to determine the incoming
 * message.
 */
abstract public class PassiveGossipThread implements Runnable {
	private final int EXTRACTION_SIZE;  //135000 for twtr 140000 for higgs 130000 for syn
	public static final Logger LOGGER = Logger.getLogger(PassiveGossipThread.class);

	/** The socket used for the passive thread of the gossip service. */
	private final DatagramSocket server;

	private final AtomicBoolean keepRunning;

	private final String cluster;

	private final ObjectMapper MAPPER = new ObjectMapper();

	private final GossipCore gossipCore;
	private final boolean compressDiscData;

	public PassiveGossipThread(GossipManager gossipManager, GossipCore gossipCore) {
		this.gossipCore = gossipCore;
		try {
			compressDiscData = gossipManager.compressDiscData;
			EXTRACTION_SIZE = gossipManager.PGT_EXTRACTION_SIZE;
			URI myURI = gossipManager.getMyself().getUri();
			LOGGER.debug("HOST: " + myURI.getHost() + " PORT: " + myURI.getPort());
			SocketAddress socketAddress = new InetSocketAddress(myURI.getHost(), myURI.getPort());
			server = new DatagramSocket(socketAddress);
			LOGGER.debug("Gossip service successfully initialized on port " + gossipManager.getMyself().getUri().getPort());
			LOGGER.debug("I am " + gossipManager.getMyself());
			cluster = gossipManager.getMyself().getClusterName();
			if (cluster == null) {
				throw new IllegalArgumentException("cluster was null");
			}
		} catch (SocketException ex) {
			LOGGER.warn(ex);
			throw new RuntimeException(ex);
		}
		keepRunning = new AtomicBoolean(true);
	}

	@Override
	public void run() {
		
		while (keepRunning.get()) {
			
			byte[] buf = null;
			int server_buf_size = GossipManager.MAX_PACKET_SIZE;
			try {
				server_buf_size = server.getReceiveBufferSize();
				buf = new byte[server_buf_size];
				
			} catch (SocketException e1) {
				e1.printStackTrace();
			}
			DatagramPacket p = new DatagramPacket(buf, buf.length);
			try {
				server.receive(p);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			byte[] data = p.getData();
			boolean isDiscData = false;
			int  packet_length = UdpUtil.readPacketLengthFromBuffer(buf);
			try {
				if (packet_length > 1 && packet_length <= GossipManager.MAX_PACKET_SIZE) {
					byte[] json_bytes = new byte[packet_length];
					for (int i = 0; i < packet_length; i++) {
						json_bytes[i] = buf[i + 4];
					}
					debug(packet_length, json_bytes);
					try {
						Base activeGossipMessage = MAPPER.readValue(json_bytes, Base.class);
						gossipCore.receive(activeGossipMessage);
					} catch (RuntimeException ex) {
						LOGGER.error("Unable to process message", ex);
						isDiscData = true;
						ex.printStackTrace();
					}

				} else {
					LOGGER.debug("The received message is not of the expected size, it could be DiSC data.");
					isDiscData = true;
				}

			} catch (IOException e) {
				LOGGER.error(e);
				keepRunning.set(false);
			}
			if (isDiscData)
				try {
					long startTime = System.currentTimeMillis();
					byte[] decompressed;
					int decompressedLength;
					if(compressDiscData){
						SnappyDecompressor decompressor = new SnappyDecompressor();
						decompressed = new byte[EXTRACTION_SIZE];
						decompressedLength = decompressor.decompress(data, 0, p.getLength(), decompressed, 0,decompressed.length);	
					}
					else{
						decompressed = data;
						decompressedLength = data.length; 
					}
	                LOGGER.debug("Node ("+ gossipCore.getManagerId() +") received DiSC data from "+p.getSocketAddress()+" ["+decompressedLength+"] bytes");

					
					long endTime = System.currentTimeMillis();
					long elapsedTime = endTime - startTime;
					LOGGER.debug("MsDecompressTime="+ elapsedTime );

					ByteArrayInputStream in = new ByteArrayInputStream(decompressed);
					ObjectInputStream is = new ObjectInputStream(in);
					DiscData rcvdData = (DiscData) is.readObject();
					gossipCore.messageQueue.add(rcvdData);
					//gossipCore.receiveDiscData(rcvdData);
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		shutdown();
	}

	private void debug(int packetLength, byte[] jsonBytes) {
		if (LOGGER.isDebugEnabled()) {
			String receivedMessage = new String(jsonBytes);
			LOGGER.debug("Received message (" + packetLength + " bytes): " + receivedMessage);
		}

	}

	public void shutdown() {
		try {
			server.close();
		} catch (RuntimeException ex) {
		}
	}

}
