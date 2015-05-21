package ch.eiafr.knx.utils;

public class IPGateway {

	private String m_KNXAddress;
	private String m_IPAddress;
	private String m_Name;
	private short m_Medium;
	
	/**
	 * Build a new KNX IP Gateway representation
	 * @param p_KNXAddress The KNX address of the gateway
	 * @param p_IPAddress The IP address of the gateway
	 * @param p_Name The name of the gateway
	 * @param p_Medium The KNX medium
	 */
	public IPGateway(String p_KNXAddress, String p_IPAddress, String p_Name, short p_Medium){
		m_KNXAddress = p_KNXAddress;
		m_IPAddress = p_IPAddress;
		m_Name = p_Name;
		m_Medium = p_Medium;
	}
	
	public String getKNXAddress() {
		return m_KNXAddress;
	}
	
	public String getIPAddress() {
		return m_IPAddress;
	}

	public String getName() {
		return m_Name;
	}

	public short getMedium() {
		return m_Medium;
	}	
}
