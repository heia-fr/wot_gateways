
package org.ws4d.coap.server.interfaces;

import org.ws4d.coap.common.CoapRequest;


/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Gerome Bovet <gerome.bovet@hefr.ch>
 */

public interface CoapServer extends CoapChannelListener {
    public CoapServer onAccept(CoapRequest request);
    public void doGet(ServerChannel channel, CoapRequest request);
    public void doPut(ServerChannel channel, CoapRequest request);
    public void doPost(ServerChannel channel, CoapRequest request);
    public void doDelete(ServerChannel channel, CoapRequest request);
	public void onSeparateResponseFailed(ServerChannel channel);
}
