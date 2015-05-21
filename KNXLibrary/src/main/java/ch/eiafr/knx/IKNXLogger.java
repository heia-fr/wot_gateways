package ch.eiafr.knx;

import java.net.SocketException;

import tuwien.auto.calimero.exception.KNXException;

public interface IKNXLogger {
	/**
	 * Initiate the KNX communication system
	 * 
	 * @param p_GatewayIP
	 *            The IP of the gateway
	 * @param p_GatewayKNXAddr
	 *            The KNX individual address of the gateway
	 * @throws KNXException
	 * @throws SocketException 
	 */
	public void initDatapointComm(String p_GatewayIP, String p_GatewayKNXAddr) throws KNXException, SocketException;
	
	/**
	 * Close the tunnel to the KNX gateway
	 */
	public void closeDatapointComm();
}
