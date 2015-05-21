/* Copyright [2011] [University of Rostock]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/

package org.ws4d.coap.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.ws4d.coap.client.BasicCoapClientChannel;
import org.ws4d.coap.client.interfaces.ClientChannel;
import org.ws4d.coap.client.interfaces.CoapClient;
import org.ws4d.coap.common.ChannelManager;
import org.ws4d.coap.common.CoapChannel;
import org.ws4d.coap.common.CoapMessage;
import org.ws4d.coap.common.SocketHandler;
import org.ws4d.coap.connection.ChannelKey;
import org.ws4d.coap.messages.AbstractCoapMessage;
import org.ws4d.coap.messages.CoapEmptyMessage;
import org.ws4d.coap.messages.CoapPacketType;
import org.ws4d.coap.server.interfaces.ServerChannel;
import org.ws4d.coap.server.interfaces.ServerChannelManager;
import org.ws4d.coap.tools.TimeoutHashMap;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Nico Laum <nico.laum@uni-rostock.de>
 * @author Gerome Bovet <gerome.bovet@hefr.ch>
 */

public class BasicMulticastSocketHandler implements SocketHandler {
	/*
	 * the socket handler has its own logger TODO: implement different socket
	 * handler for client and server channels
	 */
	private final static Logger logger = Logger
			.getLogger(BasicMulticastSocketHandler.class);
	protected WorkerThread workerThread = null;
	protected ClientChannel clientChannel;
	protected HashMap<ChannelKey, ServerChannel> serverChannels = new HashMap<ChannelKey, ServerChannel>();

	private ChannelManager channelManager = null;
	private DatagramChannel mcastChannel = null;

	public static final int UDP_BUFFER_SIZE = 66000; // max UDP size = 65535

	byte[] sendBuffer = new byte[UDP_BUFFER_SIZE];

	private int localPort;

	private InetAddress localAddr;

	public BasicMulticastSocketHandler(ChannelManager channelManager,
			int port, InetAddress multicastIP) throws IOException {
		logger.addAppender(new ConsoleAppender(new SimpleLayout()));
		// ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF:
		logger.setLevel(Level.WARN);

		NetworkInterface interf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
		//NetworkInterface interf = NetworkInterface.getByName("eth0");
		
		this.channelManager = channelManager;
		mcastChannel = DatagramChannel.open(StandardProtocolFamily.INET);
		mcastChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		mcastChannel.bind(new InetSocketAddress(port));		
		mcastChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, interf);
		mcastChannel.join(multicastIP, interf);
		mcastChannel.configureBlocking(false);

		this.localPort = mcastChannel.socket().getLocalPort();
		this.localAddr = multicastIP;

		workerThread = new WorkerThread();
		workerThread.start();
	}

	public BasicMulticastSocketHandler(ChannelManager channelManager)
			throws IOException {
		this.channelManager = channelManager;
		mcastChannel = DatagramChannel.open(StandardProtocolFamily.INET);
		mcastChannel.socket().bind(new InetSocketAddress(0)); // port can be 0,
																// then a free
																// port is
																// chosen
		this.localPort = mcastChannel.socket().getLocalPort();
		mcastChannel.configureBlocking(false);

		workerThread = new WorkerThread();
		workerThread.start();

	}

	protected class WorkerThread extends Thread {
		Selector selector = null;

		/*
		 * contains all received message keys of a remote (message id generated
		 * by the remote) to detect duplications
		 */
		TimeoutHashMap<MessageKey, Boolean> duplicateRemoteMap = new TimeoutHashMap<MessageKey, Boolean>(
				CoapMessage.ACK_RST_RETRANS_TIMEOUT_MS);
		/*
		 * contains all received message keys of the host (message id generated
		 * by the host) to detect duplications
		 */
		TimeoutHashMap<Integer, Boolean> duplicateHostMap = new TimeoutHashMap<Integer, Boolean>(
				CoapMessage.ACK_RST_RETRANS_TIMEOUT_MS);
		/*
		 * contains all messages that (possibly) needs to be retransmitted (ACK,
		 * RST)
		 */
		TimeoutHashMap<MessageKey, CoapMessage> retransMsgMap = new TimeoutHashMap<MessageKey, CoapMessage>(
				CoapMessage.ACK_RST_RETRANS_TIMEOUT_MS);
		/*
		 * contains all messages that are not confirmed yet (CON), MessageID is
		 * always generated by Host and therefore unique
		 */
		TimeoutHashMap<Integer, CoapMessage> timeoutConMsgMap = new TimeoutHashMap<Integer, CoapMessage>(
				CoapMessage.ACK_RST_RETRANS_TIMEOUT_MS);
		/* this queue handles the timeout objects in the right order */
		private PriorityQueue<TimeoutObject<Integer>> timeoutQueue = new PriorityQueue<TimeoutObject<Integer>>();

		public ConcurrentLinkedQueue<CoapMessage> sendBuffer = new ConcurrentLinkedQueue<CoapMessage>();

		/* Contains all sent messages sorted by message ID */
		long startTime;
		static final int POLLING_INTERVALL = 10000;

		ByteBuffer dgramBuffer;

		public WorkerThread() {
			dgramBuffer = ByteBuffer.allocate(UDP_BUFFER_SIZE);
			startTime = System.currentTimeMillis();
			try {
				selector = Selector.open();
				mcastChannel.register(selector, SelectionKey.OP_READ);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		public void close() {
			clientChannel = null;
			if (serverChannels != null)
				serverChannels.clear();

			try {
				mcastChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			/* TODO: wake up thread and kill it */
		}

		@Override
		public void run() {
			logger.log(Level.INFO, "Receive Thread started.");
			long waitFor = POLLING_INTERVALL;
			InetSocketAddress addr = null;

			while (mcastChannel != null) {
				/* send all messages in the send buffer */
				sendBufferedMessages();

				/* handle incoming packets */
				dgramBuffer.clear();
				try {
					addr = (InetSocketAddress) mcastChannel
							.receive(dgramBuffer);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (addr != null) {
					logger.log(Level.INFO, "handle incomming msg");
					handleIncommingMessage(dgramBuffer, addr);
				}

				/* handle timeouts */
				waitFor = handleTimeouts();

				/* TODO: find a good strategy when to update the timeout maps */
				duplicateRemoteMap.update();
				duplicateHostMap.update();
				retransMsgMap.update();
				timeoutConMsgMap.update();

				/*
				 * wait until 1. selector.wakeup() is called by sendMessage() 2.
				 * incomming packet 3. timeout
				 */
				try {
					/*
					 * FIXME: don't make a select, when something is in the
					 * sendQueue, otherwise the packet will be sent after some
					 * delay move this check and the select to a critical
					 * section
					 */
					selector.select(waitFor);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		protected synchronized void addMessageToSendBuffer(CoapMessage msg) {
			sendBuffer.add(msg);
			/* send immediately */
			selector.wakeup();
		}

		private void sendBufferedMessages() {
			CoapMessage msg = sendBuffer.poll();
			while (msg != null) {
				sendUdpMsg(msg);
				msg = sendBuffer.poll();
			}
		}

		private void handleIncommingMessage(ByteBuffer buffer,
				InetSocketAddress addr) {
			CoapMessage msg;
			try {
				msg = AbstractCoapMessage.parseMessage(buffer.array(),
						buffer.position());
			} catch (Exception e) {
				logger.warn("Received invalid message: message dropped!");
				e.printStackTrace();
				return;
			}

			CoapPacketType packetType = msg.getPacketType();
			int msgId = msg.getMessageID();
			MessageKey msgKey = new MessageKey(msgId, addr.getAddress(),
					addr.getPort());

			if (msg.isRequest()) {
				/*
				 * --- INCOMING REQUEST: This is an incoming client request with
				 * a message key generated by the remote client
				 */

				if (packetType == CoapPacketType.ACK
						|| packetType == CoapPacketType.RST) {
					logger.warn("Invalid Packet Type: Request can not be in a ACK or a RST packet");
					return;
				}

				if (packetType == CoapPacketType.CON) {
					logger.warn("Invalid Packet Type: Groupcomm messages cannot be of type CON");
					return;
				}

				/*
				 * check for duplicates and retransmit the response if a
				 * duplication is detected
				 */
				if (isRemoteDuplicate(msgKey)) {
					retransmitRemoteDuplicate(msgKey);
					return;
				}

				/* find or create server channel and handle incoming message */
				ServerChannel channel = serverChannels.get(new ChannelKey(addr
						.getAddress(), addr.getPort()));
				if (channel == null) {
					/* no server channel found -> create */
					channel = ((ServerChannelManager) channelManager)
							.createMulticastServerChannel(
									BasicMulticastSocketHandler.this, msg,
									addr.getAddress(), addr.getPort());
					if (channel != null) {
						/* add the new channel to the channel map */
						addServerChannel(channel);
						logger.info("Created new server channel.");
					} else {
						/*
						 * create failed -> server doesn't accept the connection
						 * --> send RST
						 */
						CoapChannel fakeChannel = new BasicCoapServerChannel(
								BasicMulticastSocketHandler.this, null,
								addr.getAddress(), addr.getPort());
						CoapEmptyMessage rstMsg = new CoapEmptyMessage(
								CoapPacketType.RST, msgId);
						rstMsg.setChannel(fakeChannel);
						sendMessage(rstMsg);
						return;

					}
				}
				msg.setChannel(channel);
				channel.handleMessage(msg);
				return;

			} else if (msg.isResponse()) {
				/*
				 * --- INCOMING RESPONSE: This is an incoming server response
				 * (message ID generated by host) or a separate server response
				 * (message ID generated by remote)
				 */

				if (packetType == CoapPacketType.RST) {
					logger.warn("Invalid Packet Type: RST packet must be empty");
					return;
				}

				/* check for separate response */
				if (packetType == CoapPacketType.CON) {
					logger.warn("Invalid Packet Type: Groupcomm messages cannot be of type CON");
					return;
				}

				/*
				 * normal response (ACK or NON), message id was generated by
				 * host
				 */
				if (isHostDuplicate(msgId)) {
					/* drop duplicate responses */
					return;
				}

				/* confirm the request */
				/* confirm message by removing it from the non confirmedMsgMap */
				/*
				 * Corresponding to the spec the server should be aware of a NON
				 * as answer to a CON
				 */
				timeoutConMsgMap.remove(msgId);

				if (clientChannel == null) {
					logger.warn("Could not find channel of incomming response: message dropped");
					return;
				}
				msg.setChannel(clientChannel);
				clientChannel.handleMessage(msg);
				return;

			} else if (msg.isEmpty()) {
				if (packetType == CoapPacketType.CON
						|| packetType == CoapPacketType.NON) {
					/* TODO: is this always true? */
					logger.warn("Invalid Packet Type: CON or NON packets cannot be empty");
					return;
				}

				/* ACK or RST, Message Id was generated by the host */
				if (isHostDuplicate(msgId)) {
					/* drop duplicate responses */
					return;
				}

				/* confirm */
				timeoutConMsgMap.remove(msgId);

				/* get channel */
				/* This can be an ACK/RST for a client or a server channel */
				CoapChannel channel = clientChannel;
				if (channel == null) {
					channel = serverChannels.get(new ChannelKey(addr
							.getAddress(), addr.getPort()));
				}

				if (channel == null) {
					logger.warn("Could not find channel of incomming response: message dropped");
					return;
				}

				msg.setChannel(channel);
				if (packetType == CoapPacketType.ACK) {
					/* separate response ACK */
					channel.handleMessage(msg);
					return;
				}

				if (packetType == CoapPacketType.RST) {
					/* connection closed by remote */
					channel.handleMessage(msg);
					return;
				}

			} else {
				logger.error("Invalid Message Type: not a request, not a response, not empty");
			}
		}

		private long handleTimeouts() {
			long nextTimeout = POLLING_INTERVALL;

			while (true) {
				TimeoutObject<Integer> tObj = timeoutQueue.peek();
				if (tObj == null) {
					/* timeout queue is empty */
					nextTimeout = POLLING_INTERVALL;
					break;
				}

				nextTimeout = tObj.expires - System.currentTimeMillis();
				if (nextTimeout > 0) {
					/* timeout not expired */
					break;
				}
				/*
				 * timeout expired, sendMessage will send the message and create
				 * a new timeout if the message was already confirmed,
				 * nonConfirmedMsgMap.get() will return null
				 */
				timeoutQueue.poll();
				Integer msgId = tObj.object;

				/* retransmit message after expired timeout */
				sendUdpMsg((CoapMessage) timeoutConMsgMap.get(msgId));
			}
			return nextTimeout;
		}

		private boolean isRemoteDuplicate(MessageKey msgKey) {
			if (duplicateRemoteMap.get(msgKey) != null) {
				logger.info("Detected duplicate message");
				return true;
			}
			return false;
		}

		private void retransmitRemoteDuplicate(MessageKey msgKey) {
			CoapMessage retransMsg = (CoapMessage) retransMsgMap.get(msgKey);
			if (retransMsg == null) {
				logger.warn("Detected duplicate message but no response could be found");
			} else {
				sendUdpMsg(retransMsg);
			}
		}

		private boolean isHostDuplicate(int msgId) {
			if (duplicateHostMap.get(msgId) != null) {
				logger.info("Detected duplicate message");
				return true;
			}
			return false;
		}

		private void sendUdpMsg(CoapMessage msg) {
			if (msg == null) {
				return;
			}

			CoapPacketType packetType = msg.getPacketType();
			InetAddress inetAddr = msg.getChannel().getRemoteAddress();
			int port = msg.getChannel().getRemotePort();
			int msgId = msg.getMessageID();

			if (packetType == CoapPacketType.CON) {
				/*
				 * in case of a CON this is a Request requests must be added to
				 * the timeout queue except this was the last retransmission
				 */
				if (msg.maxRetransReached()) {
					/* the connection is broken */
					timeoutConMsgMap.remove(msgId);
					msg.getChannel().lostConnection(true, false);
					return;
				}
				msg.incRetransCounterAndTimeout();
				timeoutConMsgMap.put(msgId, msg);
				TimeoutObject<Integer> tObj = new TimeoutObject<Integer>(msgId,
						msg.getTimeout() + System.currentTimeMillis());
				timeoutQueue.add(tObj);
			}

			if (packetType == CoapPacketType.ACK
					|| packetType == CoapPacketType.RST) {
				/* save this type of messages for a possible retransmission */
				retransMsgMap.put(new MessageKey(msgId, inetAddr, port), msg);
			}

			/* Nothing to do for NON */

			/* send message */
			ByteBuffer buf = ByteBuffer.wrap(msg.serialize());
			/*
			 * TODO: check if serialization could fail... then do not put it to
			 * any Map!
			 */
			try {
				/*
				 * DatagramPacket packet = new DatagramPacket(buf.array(),
				 * buf.array().length, inetAddr, port);
				 * mcastChannel.socket().send(packet);
				 */
				int bytesSent = mcastChannel.send(buf, new InetSocketAddress(
						inetAddr, port));
				logger.log(Level.INFO,
						"Send Msg with ID: " + msg.getMessageID() + " bytes: "
								+ bytesSent);
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("Send UDP message failed");
			}
		}
	}

	private class MessageKey {
		public int msgID;
		public InetAddress inetAddr;
		public int port;

		public MessageKey(int msgID, InetAddress inetAddr, int port) {
			super();
			this.msgID = msgID;
			this.inetAddr = inetAddr;
			this.port = port;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((inetAddr == null) ? 0 : inetAddr.hashCode());
			result = prime * result + msgID;
			result = prime * result + port;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MessageKey other = (MessageKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (inetAddr == null) {
				if (other.inetAddr != null)
					return false;
			} else if (!inetAddr.equals(other.inetAddr))
				return false;
			if (msgID != other.msgID)
				return false;
			if (port != other.port)
				return false;
			return true;
		}

		private BasicMulticastSocketHandler getOuterType() {
			return BasicMulticastSocketHandler.this;
		}
	}

	private class TimeoutObject<T> implements Comparable<TimeoutObject> {
		private long expires;
		private T object;

		public TimeoutObject(T object, long expires) {
			this.expires = expires;
			this.object = object;
		}

		public T getObject() {
			return object;
		}

		public int compareTo(TimeoutObject o) {
			return (int) (this.expires - o.expires);
		}
	}

	private void addServerChannel(ServerChannel channel) {
		serverChannels.put(
				new ChannelKey(channel.getRemoteAddress(), channel
						.getRemotePort()), channel);
	}

	@Override
	public int getLocalPort() {
		return localPort;
	}

	@Override
	public InetAddress getLocalAddr() {
		return localAddr;
	}

	@Override
	public void removeServerChannel(ServerChannel channel) {
		serverChannels.remove(new ChannelKey(channel.getRemoteAddress(),
				channel.getRemotePort()));
	}

	@Override
	public void close() {
		workerThread.close();
	}

	@Override
	public void sendMessage(CoapMessage message) {
		if (workerThread != null) {
			workerThread.addMessageToSendBuffer(message);
		}
	}

	@Override
	public ClientChannel connect(CoapClient client, InetAddress remoteAddress,
			int remotePort) {
		if (client == null) {
			return null;
		}

		clientChannel = new BasicCoapClientChannel(this, client, remoteAddress,
				remotePort);

		return clientChannel;
	}

	@Override
	public ChannelManager getChannelManager() {
		return this.channelManager;
	}

	@Override
	public void removeClientChannel(ClientChannel channel) {
		clientChannel = null;

	}

}
