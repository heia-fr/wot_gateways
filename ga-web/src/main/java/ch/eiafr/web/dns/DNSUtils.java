package ch.eiafr.web.dns;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.LoggerFactory;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Address;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.ResolverListener;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Update;
import org.xbill.DNS.ZoneTransferException;
import org.xbill.DNS.ZoneTransferIn;

public class DNSUtils {
	private String m_DNSIP = "192.168.1.34";
	private String m_GatewayIP = "192.168.1.35";
	private String m_DNSZone = "ch.";

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(DNSUtils.class);
	private static DNSUtils instance = null;

	private DNSUtils(String p_DNSIP, String p_GatewayIP, String p_DNSZone) {
		m_DNSIP = p_DNSIP;
		m_GatewayIP = p_GatewayIP;
		m_DNSZone = p_DNSZone;
	}

	public static DNSUtils getInstance(String p_DNSIP, String p_GatewayIP,
			String p_DNSZone) {
		if (instance == null)
			instance = new DNSUtils(p_DNSIP, p_GatewayIP, p_DNSZone);
		return instance;
	}

	public InetAddress getIpAddress(String name) throws UnknownHostException {
		InetAddress addr = Address.getByName(name);
		return addr;
	}

	public void addNewHost(final String host) throws IOException, ZoneTransferException {
		Name zone = Name.fromString(m_DNSZone + ".");		
		Name hostname = Name.fromString(host, zone);
		InetAddress address = Address.getByName(m_GatewayIP);

		Update update = new Update(zone);
		Record record = new ARecord(hostname, DClass.IN, 604800, address);

		update.add(record);

		SimpleResolver resolver = new SimpleResolver(m_DNSIP);
		resolver.sendAsync(update, new ResolverListener() {

			@Override
			public void handleException(Object arg0, Exception arg1) {
				logger.error("Unable to add host " + host, arg1);
			}

			@Override
			public void receiveMessage(Object arg0, Message arg1) {
				logger.info("Host " + host + " is added to zone " + m_DNSZone);

			}

		});

	}
}
