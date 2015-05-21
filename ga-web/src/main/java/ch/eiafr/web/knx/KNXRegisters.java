package ch.eiafr.web.knx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.datapoint.Datapoint;
import ch.eiafr.knx.IKNXManagement;
import ch.eiafr.knx.KNXManagement;

/**
 * Manage all information about the registers
 * 
 * @author sebastien baudin
 * 
 */
public class KNXRegisters {
	private static final Logger logger = LoggerFactory
			.getLogger(KNXRegisters.class);
	private IKNXManagement knxManagement = KNXManagement.getInstance();
	private static KNXRegisters knxRegisters = new KNXRegisters();
	private Map<String, List<String>> registers = new HashMap<String, List<String>>();

	private KNXRegisters() {
	}

	public static KNXRegisters getInstance() {
		return knxRegisters;
	}

	/**
	 * Add a callback url to a datapoint
	 * 
	 * @param datapoint
	 *            the datapoint to listen to
	 * @param callbackUrl
	 *            the url to connect to
	 * @param datapointUrl
	 *            the url of datapoint
	 * @throws Exception
	 *             error to register the observer
	 */
	public void addRegister(Datapoint datapoint, String callbackUrl,
			String datapointUrl) throws Exception {
		if (registers.containsKey(datapointUrl)) {
			List<String> callbacks = registers.get(datapointUrl);

			if (callbacks.size() == 0) {
				this.addKnxListener(datapoint, datapointUrl);
				callbacks.add(callbackUrl);
			} else if (callbacks.contains(callbackUrl) == false) {
				callbacks.add(callbackUrl);
			} else {
				logger.warn("The url (" + callbackUrl
						+ ") is already registered to the datapoint");
			}
		} else {
			List<String> callbacks = new ArrayList<String>();
			callbacks.add(callbackUrl);
			registers.put(datapointUrl, callbacks);
			this.addKnxListener(datapoint, datapointUrl);
		}
	}

	/**
	 * Remove a listener to a datapoint
	 * 
	 * @param callbackUrl
	 *            callback url of listener
	 * @param datapointUrl
	 *            datapoint url
	 * @throws Exception
	 *             error when deleted the listener
	 */
	public void removeRegister(String callbackUrl, String datapointUrl)
			throws Exception {

		if (registers.containsKey(datapointUrl)) {
			List<String> callbacks = registers.get(datapointUrl);
			if (callbacks.contains(callbackUrl)) {
				callbacks.remove(callbackUrl);
				if (callbacks.size() == 0) {
					this.removeKnxListener(datapointUrl);
				}
			}
		}

	}

	/**
	 * Get the list of all callback urls of a specified datapoint
	 * 
	 * @param datapointUrl
	 *            datapoint url
	 * @return list of callback urls
	 */
	public List<String> getAllRegisteredCallbacks(String datapointUrl) {
		if (registers.containsKey(datapointUrl)) {
			return registers.get(datapointUrl);
		} else
			return new ArrayList<String>();
	}

	/**
	 * Add an observer to the knx datapoint.
	 * 
	 * @param datapoint
	 * @param url
	 * @throws Exception
	 */
	private void addKnxListener(Datapoint datapoint, String url)
			throws Exception {
		try {
			knxManagement.addDatapointListener(datapoint, url);
		} catch (Exception e) {
			logger.error("Error to add the listener");
			throw e;
		}
	}

	/**
	 * Remove an observer to the knx datapoint
	 * 
	 * @param url
	 * @throws Exception
	 */
	private void removeKnxListener(String url) throws Exception {
		try {
			knxManagement.removeDatapointListener(url);
		} catch (Exception e) {
			logger.error("Error to remove the listener");
			throw e;
		}

	}

}
