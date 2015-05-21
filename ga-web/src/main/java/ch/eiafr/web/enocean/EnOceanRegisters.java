package ch.eiafr.web.enocean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage all information about the registers
 * 
 * @author Gerome Bovet
 * 
 */
public class EnOceanRegisters {
	private static final Logger logger = LoggerFactory
			.getLogger(EnOceanRegisters.class);
	private static EnOceanRegisters enoceanRegisters;
	private Map<String, List<String>> registers = new HashMap<String, List<String>>();
	private EnOceanDispatcher enoceanDispatcher;

	private EnOceanRegisters() throws Exception  {
		enoceanDispatcher = EnOceanDispatcher.getInstance();
	}

	public static EnOceanRegisters getInstance()  {
		if (enoceanRegisters == null)
			try {
				enoceanRegisters = new EnOceanRegisters();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return enoceanRegisters;
	}

	/**
	 * Add a callback url to a measure
	 * 
	 * @param idMeasure
	 *            the measure to listen to
	 * @param callbackUrl
	 *            the url to connect to
	 * @param measureUrl
	 *            the url of measure
	 * @throws Exception
	 *             error to register the observer
	 */
	public void addRegister(int idMeasure, String callbackUrl, String measureUrl)
			throws Exception {
		if (registers.containsKey(measureUrl)) {
			List<String> callbacks = registers.get(measureUrl);

			if (callbacks.size() == 0) {
				this.addEnOceanListener(idMeasure, measureUrl);
				callbacks.add(callbackUrl);
			} else if (callbacks.contains(callbackUrl) == false) {
				callbacks.add(callbackUrl);
			} else {
				logger.warn("The url (" + callbackUrl
						+ ") is already registered to the measure");
			}
		} else {
			List<String> callbacks = new ArrayList<String>();
			callbacks.add(callbackUrl);
			registers.put(measureUrl, callbacks);
			this.addEnOceanListener(idMeasure, measureUrl);
		}
	}

	/**
	 * Remove a listener to a measure
	 * 
	 * @param callbackUrl
	 *            callback url of listener
	 * @param measureUrl
	 *            measure url
	 * @throws Exception
	 *             error when deleted the listener
	 */
	public void removeRegister(String callbackUrl, String measureUrl)
			throws Exception {

		if (registers.containsKey(measureUrl)) {
			List<String> callbacks = registers.get(measureUrl);
			if (callbacks.contains(callbackUrl)) {
				callbacks.remove(callbackUrl);
				if (callbacks.size() == 0) {
					this.removeEnOceanListener(measureUrl);
				}
			}
		}

	}

	/**
	 * Get the list of all callback urls of a specified measure
	 * 
	 * @param measureUrl
	 *            measure url
	 * @return list of callback urls
	 */
	public List<String> getAllRegisteredCallbacks(String measureUrl) {
		if (registers.containsKey(measureUrl)) {
			return registers.get(measureUrl);
		} else
			return new ArrayList<String>();
	}

	/**
	 * Add an observer to the measure.
	 * 
	 * @param idMeasure
	 * @param url
	 * @throws Exception
	 */
	private void addEnOceanListener(int idMeasure, String url) throws Exception {
		try {
			enoceanDispatcher.addMeasureListener(idMeasure, url);
		} catch (Exception e) {
			logger.error("Error to add the listener");
			throw e;
		}
	}

	/**
	 * Remove an observer to the measure
	 * 
	 * @param url
	 * @throws Exception
	 */
	private void removeEnOceanListener(String url) throws Exception {
		try {
			enoceanDispatcher.removeMeasureListener(url);
		} catch (Exception e) {
			logger.error("Error to remove the listener");
			throw e;
		}

	}

}
