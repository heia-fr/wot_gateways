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

package org.ws4d.coap.client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Random;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.ws4d.coap.Constants;
import org.ws4d.coap.client.interfaces.ClientChannel;
import org.ws4d.coap.client.interfaces.CoapClient;
import org.ws4d.coap.client.interfaces.ClientChannelManager;
import org.ws4d.coap.common.SocketHandler;
import org.ws4d.coap.server.BasicCoapSocketHandler;
import org.ws4d.coap.server.BasicMulticastSocketHandler;


/**
 * @author Gerome Bovet <gerome.bovet@hefr.ch>
 */

public class BasicClientChannelManager implements ClientChannelManager {
	// global message id
	private final static Logger logger = Logger
			.getLogger(BasicClientChannelManager.class);
	private int globalMessageId;
	private static BasicClientChannelManager instance;
	private HashMap<String, SocketInformation> multicastSocketMap = new HashMap<String, SocketInformation>();
	private HashMap<Integer, SocketInformation> coapSocketMap = new HashMap<Integer, SocketInformation>();
	

	private BasicClientChannelManager() {
		logger.addAppender(new ConsoleAppender(new SimpleLayout()));
		// ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF:
		logger.setLevel(Level.WARN);
		initRandom();
	}

	public synchronized static ClientChannelManager getInstance() {
		if (instance == null) {
			instance = new BasicClientChannelManager();
		}
		return instance;
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
	public ClientChannel connectMulticast(CoapClient client,
			InetAddress addr, int port) {
		SocketHandler socketHandler = null;
		try {
			socketHandler = new BasicMulticastSocketHandler(this);
			SocketInformation sockInfo = new SocketInformation(socketHandler);
			multicastSocketMap.put(addr.toString() + ":" + socketHandler.getLocalPort(),
					sockInfo);
			return socketHandler.connect(client, addr, port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public ClientChannel connect(CoapClient client,
			InetAddress addr, int port) {
		SocketHandler socketHandler = null;
		try {
			socketHandler = new BasicCoapSocketHandler(this);
			SocketInformation sockInfo = new SocketInformation(socketHandler);
			coapSocketMap.put(socketHandler.getLocalPort(),
					sockInfo);
			return socketHandler.connect(client, addr, port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private class SocketInformation {
		public SocketHandler socketHandler = null;

		public SocketInformation(SocketHandler socketHandler) {
			super();
			this.socketHandler = socketHandler;
		}
	}

	public void remove(int key) {
		coapSocketMap.remove(key);
	}

}
