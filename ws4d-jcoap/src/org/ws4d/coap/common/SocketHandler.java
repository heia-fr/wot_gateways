package org.ws4d.coap.common;

import java.net.InetAddress;

import org.ws4d.coap.client.interfaces.ClientChannel;
import org.ws4d.coap.client.interfaces.CoapClient;
import org.ws4d.coap.server.interfaces.ServerChannel;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */

public interface SocketHandler {
    // public void registerResponseListener(CoapResponseListener
    // responseListener);
    // public void unregisterResponseListener(CoapResponseListener
    // responseListener);
    // public int sendRequest(CoapMessage request);
    // public void sendResponse(CoapResponse response);
    // public void establish(DatagramSocket socket);
    // public void testConfirmation(int msgID);
    //
    // public boolean isOpen();
    /* TODO */
    public ClientChannel connect(CoapClient client, InetAddress remoteAddress, int remotePort);

    public void close();

    public void sendMessage(CoapMessage msg);

    public ChannelManager getChannelManager();

	int getLocalPort();
	
	InetAddress getLocalAddr();

	void removeServerChannel(ServerChannel channel);

	void removeClientChannel(ClientChannel channel);
}
