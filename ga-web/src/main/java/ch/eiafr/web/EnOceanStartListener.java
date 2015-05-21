package ch.eiafr.web;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ws4d.coap.server.BasicServerChannelManager;
import org.ws4d.coap.server.interfaces.ServerChannelManager;

import ch.eiafr.enocean.EnoceanCommunicator;
import ch.eiafr.enocean.IEnoceanCommunicator;
import ch.eiafr.web.enocean.EnOceanCoapServer;
import ch.eiafr.web.enocean.EnOceanDispatcher;
import ch.eiafr.web.enocean.EnOceanStorage;
import ch.eiafr.web.enocean.admin.EnOceanConfig;

public class EnOceanStartListener implements ServletContextListener {

	private static final Logger logger = LoggerFactory
			.getLogger(EnOceanStartListener.class);

	IEnoceanCommunicator enoceanCommunicator;
	EnOceanStorage enoceanStorage = EnOceanStorage.getInstance();
	ServerChannelManager channelManager = BasicServerChannelManager
			.getInstance();

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		logger.debug("Stop listener");
		/*
		 * try { EnOceanConfig.writeConfig(); } catch (FileNotFoundException e)
		 * { logger.error("Error writing EnOcean config file", e); } catch
		 * (IOException e) { logger.error("Error writing EnOcean config file",
		 * e); }
		 */
		enoceanCommunicator.close();
		enoceanStorage.closeConnection();

	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		logger.debug("Start listener");
		try {
			EnOceanConfig.loadConfig();
		} catch (IOException e) {
			logger.error("Error reading EnOcean config file", e);
		}

		try {
			enoceanCommunicator = EnoceanCommunicator.getInstance(
					EnOceanConfig.getSerialPort(), EnOceanConfig.getEepFile());
			enoceanStorage.openConnection("com.mysql.jdbc.Driver",
					EnOceanConfig.getDBUser(), EnOceanConfig.getDBPassword(),
					EnOceanConfig.getDBUrl());
			enoceanCommunicator.addListener(EnOceanDispatcher.getInstance());
		} catch (IOException e) {
			logger.error("Error to init EnOcean", e);
		} catch (Exception e) {
			logger.error("Error (port " + EnOceanConfig.getSerialPort() + ")",
					e);
		}
		
		channelManager.createServerListener(new EnOceanCoapServer(), 5683);
		
	}
}
