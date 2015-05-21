
package org.ws4d.coap.client.interfaces;

import org.ws4d.coap.common.CoapChannel;
import org.ws4d.coap.common.CoapRequest;
import org.ws4d.coap.messages.CoapRequestCode;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */

public interface ClientChannel extends CoapChannel {
    public CoapRequest createRequest(boolean reliable, CoapRequestCode requestCode);
    public void setTrigger(Object o);
    public Object getTrigger();
}
