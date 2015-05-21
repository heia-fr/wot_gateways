
package org.ws4d.coap.client.interfaces;

import org.ws4d.coap.common.CoapResponse;
import org.ws4d.coap.server.interfaces.CoapChannelListener;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */


public interface CoapClient extends CoapChannelListener {
    public void onResponse(ClientChannel channel, CoapResponse response);
    public void onConnectionFailed(ClientChannel channel, boolean notReachable, boolean resetByServer);
}
