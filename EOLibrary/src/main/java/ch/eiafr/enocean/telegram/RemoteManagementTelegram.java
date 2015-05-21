package ch.eiafr.enocean.telegram;

import java.util.HashMap;
import java.util.Map;
/**
 * Represents a Remote management telegram, see Enocean EEP description for more information
 * @author gb
 *
 */
public class RemoteManagementTelegram {
	private int functionNumber;
	private int manufacturerId;
	private byte[] messageData;
	private Map<String, Integer> optionalData;
	
	/**
	 * Constructor
	 * @param dataBuffer The payload and optional data buffer
	 * @param dataLength The payload data size
	 * @param optionLength The optional data size
	 */
	public RemoteManagementTelegram(byte[] dataBuffer, int dataLength, int optionLength) {
		
		optionalData = new HashMap<String, Integer>();

		functionNumber = (new Integer(dataBuffer[0] << 8) | new Integer(dataBuffer[1]));
		
		manufacturerId = (new Integer(dataBuffer[2] << 8) | new Integer(dataBuffer[3]));
	
		messageData = new byte[dataLength - 4];
		for (int i = 0; i < dataLength - 4; i++)
			messageData[i] = dataBuffer[i + 4];


		if (optionLength == 10) {
			int destination = (new Integer(dataBuffer[dataLength + 1]) << 24)
					| (new Integer(dataBuffer[dataLength + 2]) << 16)
					| (new Integer(dataBuffer[dataLength + 3]) << 8)
					| new Integer(dataBuffer[dataLength + 4]);
			optionalData.put("Destination ID", destination);
			int source = (new Integer(dataBuffer[dataLength + 5]) << 24)
					| (new Integer(dataBuffer[dataLength + 6]) << 16)
					| (new Integer(dataBuffer[dataLength + 7]) << 8)
					| new Integer(dataBuffer[dataLength + 8]);
			optionalData.put("Source ID", source);
			optionalData.put("Send With Delay",
					new Integer(dataBuffer[dataLength + 9]));
		}
	}
	
	/**
	 * Get the function number
	 * @return The number
	 */
	public int getFunctionNumber() {
		return functionNumber;
	}
	
	/**
	 * Get the manufacturer Id
	 * @return The Id
	 */
	public int getManufacturerId() {
		return manufacturerId;
	}

	/**
	 * Get the message data
	 * @return The data
	 */
	public byte[] getMessageData() {
		return messageData;
	}
	
	/**
	 * Get the optional data buffer
	 * @return The buffer
	 */
	public Map<String, Integer> getOptionalData() {
		return optionalData;
	}
}
