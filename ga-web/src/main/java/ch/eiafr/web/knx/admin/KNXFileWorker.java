package ch.eiafr.web.knx.admin;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eiafr.knx.KNXManagement;
import ch.eiafr.web.dns.DNSUtils;

public class KNXFileWorker implements Runnable {

	private static final Logger logger = LoggerFactory
			.getLogger(KNXFileWorker.class);
	private boolean m_IsKNXProj;
	private String m_IPAddr;

	public KNXFileWorker(boolean p_IsKNXProj, String p_IPAddr) {
		m_IsKNXProj = p_IsKNXProj;
		m_IPAddr = p_IPAddr;
	}

	@Override
	public void run() {

		try {
			if (m_IsKNXProj)
				KNXManagement.getInstance().generateXMLDatapoints(
						KNXConfig.getDatapointFilePath() + "/archive.knxproj",
						Thread.currentThread().getContextClassLoader()
								.getResource("KNXTransformer.xsl").getFile(),
						KNXConfig.getDatapointFilePath() + "/datapoints.xml");

			KNXManagement.getInstance().initDatapointLocator(
					KNXConfig.getDatapointFilePath() + "/datapoints.xml");
		} catch (Exception e) {
			logger.error("Error while generating xml", e);
		}

		ArrayList<String> l_urls;
		try {
			l_urls = KNXManagement.getInstance().getAllUrls();
			DNSUtils l_dns = DNSUtils.getInstance(KNXConfig.getDNSIP(),
					m_IPAddr, KNXConfig.getDNSZone());

			for (int i = 0; i < l_urls.size(); i++) {
				logger.info("Adding host " + i + " of " + l_urls.size());
				l_dns.addNewHost(l_urls.get(i));
			}
		} catch (Exception e) {
			logger.error("Error getting all URLs", e);
		}
		logger.info("KNX file processed");
	}

}
