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

package org.ws4d.coap.client.interfaces;
/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Gerome Bovet <gerome.bovet@hefr.ch>
 */

import java.net.InetAddress;

import org.ws4d.coap.common.ChannelManager;


public interface ClientChannelManager extends ChannelManager {
    public int getNewMessageID();

    /* called by a client to create a connection
     * TODO: allow client to bind to a special port */
    public ClientChannel connect(CoapClient client, InetAddress addr, int port);
    
    /* called by a client to create a multicast connection
     * TODO: allow client to bind to a special port */
    public ClientChannel connectMulticast(CoapClient client, InetAddress addr, int port);
}
