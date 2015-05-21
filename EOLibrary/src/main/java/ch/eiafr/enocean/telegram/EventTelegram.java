package ch.eiafr.enocean.telegram;

import java.util.HashMap;
import java.util.Map;


/**
 * Represents a Learn telegram, see Enocean EEP description for more information
 * @author gb
 *
 */
public class EventTelegram {

	private EventCode eventCode;
	private Map<String, Integer> data;
	
	/**
	 * Constructor
	 * @param dataBuffer The payload and optional data buffer
	 * @param dataLength The payload data size
	 * @param optionLength The optional data size
	 */
	public EventTelegram(byte[] dataBuffer, int dataLength, int optionLength) {
		data = new HashMap<String, Integer>();

		switch (dataBuffer[0]) {
		case 1:
			eventCode = EventCode.SA_RECLAIM_NOT_SUCCESSFUL;
			break;

		case 2:
			eventCode = EventCode.SA_CONFIRM_LEARN;
			data.put("Priority of the postmaster candidate", new Integer(
					dataBuffer[1]));
			data.put("Manufacturer ID", new Integer(dataBuffer[2]));
			int eep = (new Integer(dataBuffer[3]) << 16)
					| (new Integer(dataBuffer[4]) << 8) | (new Integer(dataBuffer[5]));
			data.put("EEP", eep);
			data.put("RSI", new Integer(dataBuffer[6]));
			int postmaster = (new Integer(dataBuffer[7]) << 24)
					| (new Integer(dataBuffer[8]) << 16)
					| (new Integer(dataBuffer[9]) << 8) | new Integer(dataBuffer[10]);
			data.put("Postmaster Candidate ID", postmaster);
			int smartAck = (new Integer(dataBuffer[11]) << 24)
					| (new Integer(dataBuffer[12]) << 16)
					| (new Integer(dataBuffer[13]) << 8) | new Integer(dataBuffer[14]);
			data.put("Smart Ack CLientID", smartAck);
			data.put("Hop Count", new Integer(dataBuffer[15]));
			break;

		case 3:
			eventCode = EventCode.SA_LEARN_ACK;
			int time = (new Integer(dataBuffer[1]) << 8) | new Integer(dataBuffer[2]);
			data.put("Response time", time);
			data.put("Confirm code", new Integer(dataBuffer[3]));
			break;

		case 4:
			eventCode = EventCode.CO_READY;
			data.put("Reset Cause", new Integer(dataBuffer[1]));
			break;

		case 5:
			eventCode = EventCode.CO_READY_CO_EVENT_SECUREDEVICES;
			data.put("Event Cause", new Integer(dataBuffer[1]));
			int deviceID = (new Integer(dataBuffer[2]) << 24)
					| (new Integer(dataBuffer[3]) << 16)
					| (new Integer(dataBuffer[4]) << 8) | new Integer(dataBuffer[5]);
			data.put("Device ID", deviceID);
			break;
		}
	}
	
	/**
	 * Get the event code
	 * @return The event code
	 */
	public EventCode getEventCode() {
		return eventCode;
	}
	
	/**
	 * Get the map of data values
	 * @return The map
	 */
	public Map<String, Integer> getData() {
		return data;
	}
	
}
