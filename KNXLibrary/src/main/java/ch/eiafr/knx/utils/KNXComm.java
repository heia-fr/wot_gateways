package ch.eiafr.knx.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import tuwien.auto.calimero.DetachEvent;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.knxnetip.util.ManufacturerDIB;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.KNXMediumSettings;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
import tuwien.auto.calimero.process.ProcessEvent;
import tuwien.auto.calimero.process.ProcessListener;
import ch.eiafr.knx.KNXLogger;
import ch.eiafr.knx.KNXManagement;

public class KNXComm implements ProcessListener {
	private KNXNetworkLinkIP m_link = null;
	private String m_GatewayIP = "";
	private KNXMediumSettings m_settings;
	private ProcessCommunicatorImpl m_comm = null;
	private KNXManagement m_Management;
	private KNXLogger m_Logger;
	private static boolean m_ManualClose = false;
	private static KNXComm m_KNXComm = null;

	/**
	 * Open a connection with the IP gateway
	 * 
	 * @param p_GatewayIP
	 *            The IP of the gateway
	 * @param p_GatewayKNXAddr
	 *            The individual KNX address of the gateway
	 * @throws KNXException
	 * @throws SocketException
	 */
	private KNXComm(String p_GatewayIP, String p_GatewayKNXAddr)
			throws KNXException, SocketException {
		m_GatewayIP = p_GatewayIP;
		m_settings = new KNXMediumSettings(new IndividualAddress(
				p_GatewayKNXAddr)) {
			@Override
			public short getMedium() {
				return KNXMediumSettings.MEDIUM_TP1;
			}
		};

		// m_link = new KNXNetworkLinkIP(p_GatewayIP, l_settings);
		openConnection();
	}

	private void openConnection() throws SocketException, KNXException {
		m_link = new KNXNetworkLinkIP(KNXNetworkLinkIP.TUNNEL,
				new InetSocketAddress(getIPAddr(), 6378),
				new InetSocketAddress(m_GatewayIP, 3671), false, m_settings);
		m_comm = new ProcessCommunicatorImpl(m_link);
		m_comm.addProcessListener(this);
	}

	public static KNXComm getInstance(String p_GatewayIP,
			String p_GatewayKNXAddr) throws SocketException, KNXException {
		if (m_KNXComm == null || m_ManualClose) {
			m_KNXComm = new KNXComm(p_GatewayIP, p_GatewayKNXAddr);
			m_ManualClose = false;
		}

		return m_KNXComm;
	}

	private String getIPAddr() throws SocketException {
		String addr = null;
		Enumeration<NetworkInterface> interfaces = NetworkInterface
				.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface current = interfaces.nextElement();
			System.out.println(current);
			// current.getName().equals(arg0);
			if (!current.isUp() || current.isLoopback() || current.isVirtual())
				continue;
			Enumeration<InetAddress> addresses = current.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress current_addr = addresses.nextElement();
				if (current_addr.isLoopbackAddress()
						|| current_addr.isAnyLocalAddress()
						|| current_addr.isLinkLocalAddress()
						|| !(current_addr instanceof Inet4Address))
					continue;
				addr = current_addr.getHostAddress();
			}
		}
		return addr;
	}

	/**
	 * Read a value from a datapoint
	 * 
	 * @param p_dp
	 *            The datapoint to read
	 * @return The actual value
	 */
	public String readDatapoint(Datapoint p_dp) {
		try {
			if (!m_link.isOpen())
				openConnection();
			return m_comm.read(p_dp);
		} catch (KNXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Write a value to a datapoint
	 * 
	 * @param p_dp
	 *            The datapoint to write to
	 * @param p_value
	 *            The value to write
	 */
	public void writeDatapoint(Datapoint p_dp, String p_value) {
		try {
			if (!m_link.isOpen())
				openConnection();
			m_comm.write(p_dp, p_value);
		} catch (KNXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Close the connection to the IP gateway
	 */
	public void close() {
		m_ManualClose = true;
		if (m_comm != null) {
			m_comm.detach();
			m_link.close();
		}
	}

	@Override
	public void detached(DetachEvent arg0) {
		if (m_ManualClose)
			return;

		try {
			openConnection();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KNXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void groupWrite(ProcessEvent ev) {

		if (m_Logger != null)
			m_Logger.notifyListeners(ev);

		if (m_Management != null)
			m_Management.notifyListeners(ev);

	}

	/**
	 * Set the logger
	 * 
	 * @param p_Logger
	 *            The logger reference
	 */
	public void setLogger(KNXLogger p_Logger) {
		m_Logger = p_Logger;
	}

	/**
	 * Set the management
	 * 
	 * @param p_Management
	 *            The management reference
	 */
	public void setManagement(KNXManagement p_Management) {
		m_Management = p_Management;
	}

}
