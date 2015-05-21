package ch.eiafr.enocean.telegram;

import java.util.Arrays;


/**
 * Represents a Response telegram, see Enocean EEP description for more information
 * @author gb
 *
 */
public class ResponseTelegram {

	private ReturnCode returnCode;
	private byte[] optionalData;
	private byte[] data;

	/**
	 * Constructor
	 * @param dataBuffer The payload and optional data buffer
	 * @param dataLength The payload data size
	 * @param optionLength The optional data size
	 */
	public ResponseTelegram(byte[] dataBuffer, int dataLength, int optionLength) {

		switch (dataBuffer[0]) {
		case 0:
			returnCode = ReturnCode.RET_OK;
			break;
		case 1:
			returnCode = ReturnCode.RET_ERROR;
			break;
		case 2:
			returnCode = ReturnCode.RET_NOT_SUPPORTED;
			break;
		case 3:
			returnCode = ReturnCode.RET_WRONG_PARAM;
			break;
		case 4:
			returnCode = ReturnCode.RET_OPERATION_DENIED;
			break;
		}
		optionalData = new byte[optionLength];
		byte[] data = dataBuffer;
		for (int i = 0; i < optionLength; i++)
			optionalData[i] = data[i + 1];
		
		this.data = Arrays.copyOfRange(dataBuffer, 1, dataLength-optionLength+1);
	}

	/**
	 * Get the response return code
	 * @return The code
	 */
	public ReturnCode getReturnCode() {
		return returnCode;
	}

	/**
	 * Get the optional data buffer
	 * @return The buffer
	 */
	public byte[] getOptionalData() {
		return optionalData;
	}

	/**
	 * Get the data buffer. Not containing the response code
	 * @return The buffer
	 */
	public byte[] getData() {
		return data;
	}

}
