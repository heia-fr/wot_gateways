package ch.eiafr.web.dns;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.jdom2.JDOMException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xbill.DNS.ZoneTransferException;

import ch.eiafr.knx.IKNXManagement;
import ch.eiafr.knx.KNXManagement;

@RunWith(value = Parameterized.class)
public class DNSUtilsTests {
	private DNSUtils dnsUtils = DNSUtils.getInstance("192.168.1.34", "192.168.1.35", "ch.");
	private IKNXManagement knxManagement = KNXManagement.getInstance();

	private int number;

	public DNSUtilsTests(int number) {
		this.number = number;
	}

	@Parameters
	public static Collection<Object[]> data1() {
		Object[][] data = { { 1 } };

		return Arrays.asList(data);
	}

	@Test
	public void testGetIpAddress() {
		try {
			InetAddress address = dnsUtils
					.getIpAddress("test.eia-fr.ch");
			Assert.assertEquals("192.168.1.35", address.getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testAddNewHost() {
		try {
			String host = "testAdd.eia-fr";
			try {
				dnsUtils.addNewHost(host);
			} catch (ZoneTransferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			InetAddress address = dnsUtils.getIpAddress(host + ".ch");
			Assert.assertEquals("192.168.1.35", address.getHostAddress());
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testAddHostsFromXml() {
		String xmlPath = "/Users/sebastienbaudin/git/wattict/EIAFR.xml";
		try {
			knxManagement.initDatapointLocator(xmlPath);
		} catch (JDOMException e) {
			Assert.fail();
			e.printStackTrace();
		} catch (IOException e) {
			Assert.fail();
			e.printStackTrace();
		}

		try {
			List<String> hosts = knxManagement.getAllUrls();

			for (String host : hosts) {
				dnsUtils.addNewHost(host);
				InetAddress address = dnsUtils
						.getIpAddress(host + ".ch");
				Assert.assertEquals("192.168.1.35", address.getHostAddress());
			}
		} catch (Exception e) {
			Assert.fail();
			e.printStackTrace();
		}
	}
}
