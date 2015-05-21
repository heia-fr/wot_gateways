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

package org.ws4d.coap.server.interfaces;
/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */

import java.net.InetAddress;

import org.ws4d.coap.common.ChannelManager;
import org.ws4d.coap.common.CoapMessage;
import org.ws4d.coap.common.SocketHandler;



public interface ServerChannelManager extends ChannelManager{
    public int getNewMessageID();

    /* called by the socket Listener to create a new Server Channel
     * the Channel Manager then asked the Server Listener if he wants to accept a new connection */
	public ServerChannel createServerChannel(SocketHandler socketHandler, CoapMessage message, InetAddress addr, int port);
	
	/* called by the socket Listener to create a new multicast Server Channel
     * the Channel Manager then asked the Server Listener if he wants to accept a new connection */
	public ServerChannel createMulticastServerChannel(SocketHandler socketHandler, CoapMessage message, InetAddress addr, int port);

	/* creates a multicast server socket listener for incoming connections */
    public void createMulticastServerListener(CoapServer serverListener, int localPort, InetAddress multicastIP);
    
    /* creates a server socket listener for incoming connections */
    public void createServerListener(CoapServer serverListener, int localPort);
}
