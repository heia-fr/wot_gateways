package ch.eiafr.web;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.exception.KNXException;
import ch.eiafr.knx.IKNXManagement;
import ch.eiafr.knx.KNXLogger;
import ch.eiafr.knx.KNXManagement;
import ch.eiafr.web.knx.KNXObserver;
import ch.eiafr.web.knx.KNXStorage;
import ch.eiafr.web.knx.admin.KNXConfig;

public class KNXStartListener implements ServletContextListener {

	private static final Logger logger = LoggerFactory
			.getLogger(KNXStartListener.class);

	IKNXManagement knxManagement = KNXManagement.getInstance();
	KNXStorage knxStorage = KNXStorage.getInstance();
	KNXLogger knxLogger = KNXLogger.getInstance();

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		logger.debug("Stop listener");
		try {
			KNXConfig.writeConfig();
		} catch (FileNotFoundException e) {
			logger.error("Error writing KNX config file", e);
		} catch (IOException e) {
			logger.error("Error writing KNX config file", e);
		}
		knxManagement.closeDatapointComm();
		knxStorage.closeConnection();

	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		logger.debug("Start listener");
		try {
			KNXConfig.loadConfig();
		} catch (IOException e) {
			logger.error("Error reading KNX config file", e);
		}
		
		KNXObserver observer = new KNXObserver();
		try {
			knxStorage.openConnection("com.mysql.jdbc.Driver", KNXConfig.getDBUser(), KNXConfig.getDBPassword(), KNXConfig.getDBUrl());
			knxManagement.initDatapointLocator(KNXConfig.getDatapointFilePath()+"/datapoints.xml");
			knxManagement.initDatapointComm(KNXConfig.getIPGateway(), KNXConfig.getKNXAddress());
			knxManagement.addObserver(observer);
				
			
			knxLogger.initDatapointComm(KNXConfig.getIPGateway(), KNXConfig.getKNXAddress());
			knxLogger.addObserver(knxStorage);
		} catch (JDOMException e) {
			logger.error("Error to init knx", e);
		} catch (IOException e) {
			logger.error("Error to init knx", e);
		} catch (KNXException e) {
			logger.error("Error to init datapoint comm", e);
		} catch (Exception e) {
			logger.error("Error to generate XML file", e);
		}
		
		/*Properties prop = new Properties();
		try {
			prop.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(DefaultConfig.PROPERTIES_FILE));
		} catch (IOException e) {
			logger.error("Impossible to load properties file", e);
			System.exit(1);
		}
		
		String multicastIp = prop.getProperty("DEFAULT_IP");
		int port = Integer.parseInt(prop.getProperty("DEFAULT_PORT"));
		String type = prop.getProperty("TYPE");

		Manager multicast = new Manager(multicastIp, port, type);
		multicast.run();*/
	}
	
	private void startModules(){
		
	}

}
