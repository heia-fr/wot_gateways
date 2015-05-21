package ch.eiafr.enocean.thread;

/**
 * Raw telegram, before interpretation of its content
 * @author gb
 *
 */

public class RawTelegram {

	public static final int BUFFER_SIZE = 512;
	
	private byte[] dataBuffer = new byte[BUFFER_SIZE];
	private byte[] rawBuffer = new byte[BUFFER_SIZE];
	
	/**
	 * Get the entire telegram buffer
	 * @return The raw buffer
	 */
	public byte[] getRawBuffer() {
		return rawBuffer;
	}
	
	/**
	 * Set the entire telegram buffer
	 * @param rawBuffer The raw buffer
	 */
	public void setRawBuffer(byte[] rawBuffer) {
		this.rawBuffer = rawBuffer;
	}
	
	/**
	 * Get the length of the payload data
	 * @return The payload data size
	 */
	public int getDataLength() {
		int dataLength = rawBuffer[0];
		dataLength = dataLength << 8;
		dataLength = dataLength | (int)rawBuffer[1];
		return dataLength;
	}
	
	/**
	 * Get the length of the optinal payload data
	 * @return The optional payload data size
	 */
	public int getOptionLength() {
		return rawBuffer[2];
	}
	
	/**
	 * Get the type of frame
	 * @return Type of frame
	 */
	public int getType() {
		return rawBuffer[3];
	}
	
	/**
	 * Get the payload and optional data buffer
	 * @return The buffer
	 */
	public byte[] getDataBuffer() {
		return dataBuffer;
	}
	
	/**
	 * Set the payload and optinal data buffer
	 * @param dataBuffer The buffer
	 */
	public void setDataBuffer(byte[] dataBuffer) {
		this.dataBuffer = dataBuffer;
	}
	
	
}
