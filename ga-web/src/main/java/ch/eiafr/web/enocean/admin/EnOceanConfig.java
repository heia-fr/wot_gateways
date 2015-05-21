package ch.eiafr.web.enocean.admin;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class EnOceanConfig {
	private static String m_DBUser;
	private static String m_DBPassword;
	private static String m_DBUrl;
	private static String m_DNSIP;
	private static String m_DNSZone;
	private static String m_serialPort;
	private static String m_eepFile;
	
	public EnOceanConfig() {
	}

	public static String getDBUser() {
		return m_DBUser;
	}

	public static void setDBUser(String p_DBUser) {
		m_DBUser = p_DBUser;
	}

	public static String getDBPassword() {
		return m_DBPassword;
	}

	public static void setDBPassword(String p_DBPassword) {
		m_DBPassword = p_DBPassword;
	}

	public static String getDBUrl() {
		return m_DBUrl;
	}

	public static void setDBUrl(String p_DBUrl) {
		m_DBUrl = p_DBUrl;
	}

	public static String getDNSIP() {
		return m_DNSIP;
	}

	public static void setDNSIP(String p_DNSIP) {
		m_DNSIP = p_DNSIP;
	}

	public static String getDNSZone() {
		return m_DNSZone;
	}

	public static void setDNSZone(String p_DNSZone) {
		m_DNSZone = p_DNSZone;
	}

	public static String getSerialPort() {
		return m_serialPort;
	}

	public static void setSerialPort(String p_serialPort) {
		EnOceanConfig.m_serialPort = p_serialPort;
	}

	public static String getEepFile() {
		return m_eepFile;
	}

	public static void setEepFile(String p_eepFile) {
		EnOceanConfig.m_eepFile = p_eepFile;
	}

	public static void loadConfig() throws IOException {
		Properties l_properties = new Properties();
		l_properties.load(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("enocean.properties"));

		m_DNSIP = l_properties.getProperty("dns.ip");
		m_DNSZone = l_properties.getProperty("dns.zone");

		m_DBUser = l_properties.getProperty("db.user");
		m_DBPassword = l_properties.getProperty("db.password");
		m_DBUrl = l_properties.getProperty("db.url");

		m_serialPort = l_properties.getProperty("serialPort");
		m_eepFile = l_properties.getProperty("eepFile");
	}

	public static void writeConfig() throws IOException {
		Properties l_properties = new Properties();

		l_properties.setProperty("dns.ip", m_DNSIP);
		l_properties.setProperty("dns.zone", m_DNSZone);

		l_properties.setProperty("db.user", m_DBUser);
		l_properties.setProperty("db.password", m_DBPassword);
		l_properties.setProperty("db.url", m_DBUrl);

		l_properties.setProperty("serialPort", m_serialPort);
		l_properties.setProperty("eepFile", m_eepFile);

		FileOutputStream l_Out = new FileOutputStream(Thread.currentThread()
				.getContextClassLoader().getResource("enocean.properties")
				.getFile());
		l_properties.store(l_Out, null);
		l_Out.close();
	}
}
