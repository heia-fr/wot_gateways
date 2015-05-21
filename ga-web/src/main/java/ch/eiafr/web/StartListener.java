package ch.eiafr.web;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eiafr.MulticastServer.Manager;
import ch.eiafr.device.PowerException;
import ch.eiafr.device.PowerManager;
import ch.eiafr.logger.service.SensorService;

/**
 * Entry point of Java Web App
 * 
 * @author Sebastien Baudin
 * 
 */
public class StartListener implements ServletContextListener {
	private static final Logger logger = LoggerFactory
			.getLogger(StartListener.class);

	private PowerManager pm;

	private SensorService sensorService = SensorService.getInstance();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("Servlet context initialized");

		Properties prop = new Properties();
		try {
			prop.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(DefaultConfig.PROPERTIES_FILE));
		} catch (IOException e) {
			logger.error("Impossible to load properties file", e);
			System.exit(1);
		}

		String portName = prop.getProperty("serial.port",
				DefaultConfig.SERIAL_PORT);
		int discoverTime = Integer.valueOf(prop.getProperty("discover.time",
				String.valueOf(DefaultConfig.DISCOVER_TIME)));
		int requestTime = Integer.valueOf(prop.getProperty("request.time",
				String.valueOf(DefaultConfig.REQUEST_TIME)));
		boolean reset = Boolean.valueOf(System.getProperty("reset.key",
				String.valueOf(DefaultConfig.RESET_KEY)));
		pm = new PowerManager(portName, discoverTime, requestTime, reset);

		pm.registerListener(SensorService.getInstance());

		String driver = DefaultConfig.DATABASE_DRIVER;
		String url = prop.getProperty("db.url", DefaultConfig.DB_URL);
		String user = prop.getProperty("db.user", DefaultConfig.DB_USER);
		String password = prop.getProperty("db.password",
				DefaultConfig.DB_PASSWORD);

		sensorService.openDatabaseConnection(driver, url, user, password);

		try {
			pm.start();
		} catch (PowerException e) {
			logger.error("Error to start PowerManager", e);
		} catch (IOException e) {
			logger.error("Error to start PowerManager", e);
		} catch (InterruptedException e) {
			logger.error("Error to start PowerManager", e);
		}

		String multicastIp = prop.getProperty("DEFAULT_IP");
		int port = Integer.parseInt(prop.getProperty("DEFAULT_PORT"));
		String type = prop.getProperty("TYPE");

		Manager multicast = new Manager(multicastIp, port, type);
		multicast.run();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("Servlet context destroyed");
		sensorService.closeDatabaseConnection();
		try {
			pm.stop();
		} catch (PowerException e) {
			logger.error("Error to stop PowerManager", e);
		}

	}

}
