package ch.eiafr.enocean.telegram;


/**
 * Represents a Radio Learn telegram, see Enocean EEP description for more information
 * @author gb
 *
 */
public class LearnTelegram extends RadioTelegram {
	private int EEPR_ORG;
	private int EEPFunction;
	private int EEPType;
	private int manufacturerId;

	/**
	 * Constructor
	 * @param dataBuffer The payload and optional data buffer
	 * @param dataLength The payload data size
	 * @param optionLength The optional data size
	 */
	public LearnTelegram(byte[] dataBuffer, int dataLength, int optionLength) {
		super(dataBuffer, dataLength, optionLength);
		
		if(getRORG() == R_ORG.FOUR_BS)
			EEPR_ORG = 0xa5;
		else 
			EEPR_ORG = 0xd5;
		
		if(getRORG() == R_ORG.FOUR_BS && (dataBuffer[4] & 0x08) == 0){
			EEPFunction = (dataBuffer[1] & 0xfc) >> 2;
			EEPType = (((int)dataBuffer[2]) & 0xf8) >> 3 | ((int)(dataBuffer[1]) & 0x03) << 6;
			manufacturerId = (((int)dataBuffer[2]) & 0x07) << 8 | (int)dataBuffer[3];
		}
		
	}

	/**
	 * Get the RORG number
	 * @return The RORG number
	 */
	public int getRORGNumber() {
		return EEPR_ORG;
	}

	/**
	 * Get the EEP function
	 * @return The function number
	 */
	public int getFunction() {
		return EEPFunction;
	}

	/**
	 * Get the EEP type
	 * @return The type number
	 */
	public int getType() {
		return EEPType;
	}

	/**
	 * Get the manufacturer Id
	 * @return The manufacturer Id
	 */
	public int getManufacturerId() {
		return manufacturerId;
	}
}
