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
import java.util.HashMap;
import java.util.Random;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.ws4d.coap.Constants;
import org.ws4d.coap.common.CoapMessage;
import org.ws4d.coap.common.SocketHandler;
import org.ws4d.coap.messages.BasicCoapRequest;
import org.ws4d.coap.server.interfaces.CoapServer;
import org.ws4d.coap.server.interfaces.ServerChannel;
import org.ws4d.coap.server.interfaces.ServerChannelManager;

/**
 * @author Gerome Bovet <gerome.bovet@hefr.ch>
 */

public class BasicServerChannelManager implements ServerChannelManager {
	// global message id
	private final static Logger logger = Logger
			.getLogger(BasicServerChannelManager.class);
	private int globalMessageId;
	private static BasicServerChannelManager instance;
	private HashMap<String, SocketInformation> multicastSocketMap = new HashMap<String, SocketInformation>();
	private HashMap<Integer, SocketInformation> coapSocketMap = new HashMap<Integer, SocketInformation>();
	CoapServer serverListener = null;

	private BasicServerChannelManager() {
		logger.addAppender(new ConsoleAppender(new SimpleLayout()));
		// ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF:
		logger.setLevel(Level.WARN);
		initRandom();
	}

	public synchronized static ServerChannelManager getInstance() {
		if (instance == null) {
			instance = new BasicServerChannelManager();
		}
		return instance;
	}

	/**
	 * Creates a new server channel
	 */
	@Override
	public synchronized ServerChannel createMulticastServerChannel(
			SocketHandler socketHandler, CoapMessage message, InetAddress addr,
			int port) {
		SocketInformation socketInfo = multicastSocketMap.get(socketHandler
				.getLocalAddr() + ":" + socketHandler.getLocalPort());

		if (socketInfo.serverListener == null) {
			/* this is not a server socket */
			throw new IllegalStateException("Invalid server socket");
		}

		if (!message.isRequest()) {
			throw new IllegalStateException(
					"Incomming message is not a request message");
		}

		CoapServer server = socketInfo.serverListener
				.onAccept((BasicCoapRequest) message);
		if (server == null) {
			/* Server rejected channel */
			return null;
		}
		ServerChannel newChannel = new BasicCoapServerChannel(socketHandler,
				server, addr, port);
		return newChannel;
	}

	/**
	 * Creates a new server channel
	 */
	@Override
	public synchronized ServerChannel createServerChannel(
			SocketHandler socketHandler, CoapMessage message, InetAddress addr,
			int port) {
		SocketInformation socketInfo = coapSocketMap.get(socketHandler
				.getLocalPort());

		if (socketInfo.serverListener == null) {
			/* this is not a server socket */
			throw new IllegalStateException("Invalid server socket");
		}

		if (!message.isRequest()) {
			throw new IllegalStateException(
					"Incomming message is not a request message");
		}

		CoapServer server = socketInfo.serverListener
				.onAccept((BasicCoapRequest) message);
		if (server == null) {
			/* Server rejected channel */
			return null;
		}
		ServerChannel newChannel = new BasicCoapServerChannel(socketHandler,
				server, addr, port);
		return newChannel;
	}

	/**
	 * Creates a new, global message id for a new COAP message
	 */
	@Override
	public synchronized int getNewMessageID() {
		if (globalMessageId < Constants.MESSAGE_ID_MAX) {
			++globalMessageId;
		} else
			globalMessageId = Constants.MESSAGE_ID_MIN;
		return globalMessageId;
	}

	@Override
	public synchronized void initRandom() {
		// generate random 16 bit messageId
		Random random = new Random();
		globalMessageId = random.nextInt(Constants.MESSAGE_ID_MAX + 1);
	}

	@Override
	public void createMulticastServerListener(CoapServer serverListener,
			int localPort, InetAddress multicastIP) {
		if (!multicastSocketMap.containsKey(multicastIP.toString() + ":"
				+ localPort)) {
			try {
				SocketInformation socketInfo = new SocketInformation(
						new BasicMulticastSocketHandler(this, localPort,
								multicastIP), serverListener);
				multicastSocketMap.put(
						multicastIP.toString() + ":" + localPort, socketInfo);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			/* TODO: raise exception: address already in use */
			throw new IllegalStateException();
		}
	}

	@Override
	public void createServerListener(CoapServer serverListener, int localPort) {
		if (!coapSocketMap.containsKey(localPort)) {
			try {
				SocketInformation socketInfo = new SocketInformation(
						new BasicCoapSocketHandler(this, localPort),
						serverListener);
				coapSocketMap.put(localPort, socketInfo);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			/* TODO: raise exception: address already in use */
			throw new IllegalStateException();
		}
	}

	private class SocketInformation {
		public SocketHandler socketHandler = null;
		public CoapServer serverListener = null;

		public SocketInformation(SocketHandler socketHandler,
				CoapServer serverListener) {
			super();
			this.socketHandler = socketHandler;
			this.serverListener = serverListener;
		}
	}

}
