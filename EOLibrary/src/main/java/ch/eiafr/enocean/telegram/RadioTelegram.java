package ch.eiafr.enocean.telegram;

import java.util.Arrays;

/**
 * Represents a Radio telegram, see Enocean EEP description for more information
 * 
 * @author gb
 * 
 */
public class RadioTelegram {
	private byte R_ORG;
	private String senderId = "";
	private byte[] dataBuffer;
	private int dataLength;
	private int optionLength;
	private byte status;

	/**
	 * Constructor
	 * 
	 * @param dataBuffer
	 *            The payload and optional data buffer
	 * @param dataLength
	 *            The payload data size
	 * @param optionLength
	 *            The optional data size
	 */
	public RadioTelegram(byte[] dataBuffer, int dataLength, int optionLength) {
		this.dataBuffer = dataBuffer;
		this.dataLength = dataLength;
		this.optionLength = optionLength;

		byte[] sender = new byte[4];

		R_ORG = dataBuffer[0];
		status = dataBuffer[dataLength - 1];

		sender = Arrays.copyOfRange(dataBuffer, dataLength - 5, dataLength - 1);
		for (int i = 0; i < sender.length; i++)
			senderId += String.format("%02X", sender[i]);
	}

	/**
	 * Get the RORG number
	 * 
	 * @return The RORG number
	 */
	public byte getRORG() {
		return R_ORG;
	}

	/**
	 * Get the sender Id
	 * 
	 * @return The sender Id
	 */
	public String getSenderID() {
		return senderId;
	}

	/**
	 * Get the status field
	 * 
	 * @return The status field
	 */
	public byte getStatus(){
		return status;
	}
	
	/**
	 * Get the length of the payload data
	 * 
	 * @return The data length
	 */
	public int getDataLength() {
		return dataLength;
	}

	/**
	 * Get the length of the optional data
	 * 
	 * @return The optional data length
	 */
	public int getOptionLength() {
		return optionLength;
	}

	public byte[] getDataBuffer() {
		return dataBuffer;
	}

}
