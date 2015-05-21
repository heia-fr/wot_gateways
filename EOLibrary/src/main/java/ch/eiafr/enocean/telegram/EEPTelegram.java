package ch.eiafr.enocean.telegram;

import java.util.Map;

/**
 * Represents a decoded Radio Telegram, see Enocean EEP description for more information
 * @author gb
 *
 */
public class EEPTelegram extends RadioTelegram{
	private Map<String, Double> data;
	private Map<String, Double> optionalData;

	/**
	 * Constructor
	 * @param dataBuffer The payload and optional data buffer
	 * @param dataLength The payload data size
	 * @param optionLength The optional data size
	 */
	public EEPTelegram(byte[] dataBuffer, int dataLength, int optionLength) {
		super(dataBuffer, dataLength, optionLength);
			
	}

	/**
	 * Get the data decoded according to the EEP, Key is shortcut
	 * @return The Map of shortcut and value
	 */
	public Map<String, Double> getData() {
		return data;
	}

	/**
	 * Set the data decoded according to the EEP, Key is shortcut
	 * @param data The Map of shortcut and value
	 */
	public void setData(Map<String, Double> data) {
		this.data = data;
	}

	/**
	 * Get the optional data decoded according to the EEP, Key is shortcut
	 * @return The Map of shortcut and value
	 */
	public Map<String, Double> getOptionalData() {
		return optionalData;
	}

	/**
	 * Set the optional data decoded according to the EEP, Key is shortcut
	 * @param optionalData The Map of shortcut and value
	 */
	public void setOptionalData(Map<String, Double> optionalData) {
		this.optionalData = optionalData;
	}


}
