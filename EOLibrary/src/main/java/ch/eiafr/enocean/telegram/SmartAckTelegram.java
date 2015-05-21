package ch.eiafr.enocean.telegram;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a SmartAck telegram, see Enocean EEP description for more information
 * @author gb
 *
 */
public class SmartAckTelegram {

	private SmartAckCommand smartAckCommand;
	private Map<String, Integer> data;

	/**
	 * Constructor
	 * @param dataBuffer The payload and optional data buffer
	 * @param dataLength The payload data size
	 * @param optionLength The optional data size
	 */
	public SmartAckTelegram(byte[] dataBuffer, int dataLength, int optionLength) {
		 data = new HashMap<String, Integer>();

		switch (dataBuffer[0]) {
		case 1:
			smartAckCommand = SmartAckCommand.SA_WR_LEARNMODE;
			data.put("Enable", new Integer(dataBuffer[1]));
			data.put("Extended", new Integer(dataBuffer[2]));
			int timeout = (new Integer(dataBuffer[3]) << 24)
					| (new Integer(dataBuffer[4]) << 16)
					| (new Integer(dataBuffer[5]) << 8) | new Integer(dataBuffer[6]);
			data.put("Timeout", timeout);
			break;

		case 2:
			smartAckCommand = SmartAckCommand.SA_RD_LEARNMODE;
			break;

		case 3:
			smartAckCommand = SmartAckCommand.SA_WR_LEARNCONFIRM;
			int time = (new Integer(dataBuffer[1]) << 8) | new Integer(dataBuffer[2]);
			data.put("Response time", time);
			data.put("Confirm code", new Integer(dataBuffer[3]));
			int postmaster = (new Integer(dataBuffer[4]) << 24)
					| (new Integer(dataBuffer[5]) << 16)
					| (new Integer(dataBuffer[6]) << 8) | new Integer(dataBuffer[7]);
			data.put("Postmaster Candidate ID", postmaster);
			int client = (new Integer(dataBuffer[8]) << 24)
					| (new Integer(dataBuffer[9]) << 16)
					| (new Integer(dataBuffer[10]) << 8) | new Integer(dataBuffer[11]);
			data.put("Smart Ack Client ID", client);
			break;

		case 4:
			smartAckCommand = SmartAckCommand.SA_WR_CLIENTLEARNRQ;
			data.put("Manufacturer ID", new Integer(((dataBuffer[1]) << 2) & 31
					| dataBuffer[2]));
			int eep = (new Integer(dataBuffer[3]) << 16)
					| (new Integer(dataBuffer[4]) << 8) | (new Integer(dataBuffer[5]));
			data.put("EEP", eep);
			break;

		case 5:
			smartAckCommand = SmartAckCommand.SA_WR_RESET;
			int clientR = (new Integer(dataBuffer[1]) << 24)
					| (new Integer(dataBuffer[2]) << 16)
					| (new Integer(dataBuffer[3]) << 8) | new Integer(dataBuffer[4]);
			data.put("Smart Ack Client ID", clientR);
			break;

		case 6:
			smartAckCommand = SmartAckCommand.SA_RD_LEARNEDCLIENTS;
			break;

		case 7:
			smartAckCommand = SmartAckCommand.SA_WR_RECLAIMS;
			data.put("Reclaim count", new Integer(dataBuffer[1]));
			break;

		case 8:
			smartAckCommand = SmartAckCommand.SA_WR_POSTMASTER;
			data.put("Mailbox count", new Integer(dataBuffer[1]));
			break;
		}
	}

	/**
	 * Get the data map from the telegram
	 * @return The map
	 */
	public Map<String, Integer> getData() {
		return data;
	}

	private Map<String, Integer> optionalData;

	/**
	 * Get the command type
	 * @return The command type
	 */
	public SmartAckCommand getSmartAckCommand() {
		return smartAckCommand;
	}

	/**
	 * Get the optional data map from the telegram
	 * @return The map
	 */
	public Map<String, Integer> getOptionalData() {
		return optionalData;
	}

}
