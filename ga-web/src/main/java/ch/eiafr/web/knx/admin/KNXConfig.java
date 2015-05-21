package ch.eiafr.web.knx.admin;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class KNXConfig {

	private static String m_Password;
	private static String m_IPGateway;
	private static String m_KNXAddress;
	private static String m_DNSIP;
	private static String m_DNSZone;
	private static boolean m_HasStorage;
	private static String m_DBUser;
	private static String m_DBPassword;
	private static String m_DBUrl;
	private static String m_DatapointFilePath;
	private static int m_MaxCharsLogRead;
	
	
	public static void loadConfig() throws IOException{
		Properties l_Prop = new Properties();
		l_Prop.load(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("knx.properties"));
		
		m_Password = l_Prop.getProperty("password");
		m_IPGateway = l_Prop.getProperty("knx.ip");
		m_KNXAddress = l_Prop.getProperty("knx.addr");
		m_DNSIP = l_Prop.getProperty("dns.ip");
		m_DNSZone = l_Prop.getProperty("dns.zone");
		m_HasStorage = Boolean.parseBoolean(l_Prop.getProperty("storage"));
		m_DBUser = l_Prop.getProperty("db.user");
		m_DBPassword = l_Prop.getProperty("db.password");
		m_DBUrl = l_Prop.getProperty("db.url");
		m_DatapointFilePath = l_Prop.getProperty("datapointFilePath");
		m_MaxCharsLogRead = Integer.parseInt(l_Prop.getProperty("maxCharsLogRead"));
	}
	
	public static void writeConfig() throws FileNotFoundException, IOException{
		Properties l_Prop = new Properties();
		l_Prop.setProperty("password", m_Password);
		l_Prop.setProperty("knx.ip", m_IPGateway);
		l_Prop.setProperty("knx.addr", m_KNXAddress);
		l_Prop.setProperty("dns.ip", m_DNSIP);
		l_Prop.setProperty("dns.zone", m_DNSZone);
		l_Prop.setProperty("storage", Boolean.toString(m_HasStorage));
		l_Prop.setProperty("db.user", m_DBUser);
		l_Prop.setProperty("db.password", m_DBPassword);
		l_Prop.setProperty("db.url", m_DBUrl);
		l_Prop.setProperty("datapointFilePath", m_DatapointFilePath);
		l_Prop.setProperty("maxCharsLogRead", String.valueOf(m_MaxCharsLogRead));
		FileOutputStream l_Out = new FileOutputStream(Thread.currentThread().getContextClassLoader().getResource("knx.properties").getFile());
		l_Prop.store(l_Out, null);
		l_Out.close();
	}
	
	public static String getPassword() {
		return m_Password;
	}
	public static void setPassword(String p_Password) {
		KNXConfig.m_Password = p_Password;
	}
	public static String getIPGateway() {
		return m_IPGateway;
	}
	public static void setIPGateway(String p_IPGateway) {
		KNXConfig.m_IPGateway = p_IPGateway;
	}
	public static String getKNXAddress() {
		return m_KNXAddress;
	}
	public static void setKNXAddress(String p_KNXAddress) {
		KNXConfig.m_KNXAddress = p_KNXAddress;
	}
	public static void setMaxCHarsLogRead(int p_MaxLinesLogRead) {
		KNXConfig.m_MaxCharsLogRead = p_MaxLinesLogRead;
	}
	public static String getDNSIP() {
		return m_DNSIP;
	}
	public static void setDNSIP(String p_DNSIP) {
		KNXConfig.m_DNSIP = p_DNSIP;
	}
	public static String getDNSZone() {
		return m_DNSZone;
	}
	public static void setDNSZone(String p_DNSZone) {
		KNXConfig.m_DNSZone = p_DNSZone;
	}
	public static boolean hasStorage() {
		return m_HasStorage;
	}
	public static void hasStorage(boolean p_HasStorage) {
		KNXConfig.m_HasStorage = p_HasStorage;
	}
	public static String getDBUser() {
		return m_DBUser;
	}
	public static void setDBUser(String p_DBUser) {
		KNXConfig.m_DBUser = p_DBUser;
	}
	public static String getDBPassword() {
		return m_DBPassword;
	}
	public static void setDBPassword(String p_DBPassword) {
		KNXConfig.m_DBPassword = p_DBPassword;
	}
	public static String getDBUrl(){
		return m_DBUrl;
	}
	public static String getDatapointFilePath(){
		return m_DatapointFilePath;
	}
	public static int getMaxCharsLogRead(){
		return m_MaxCharsLogRead;
	}
	
}
