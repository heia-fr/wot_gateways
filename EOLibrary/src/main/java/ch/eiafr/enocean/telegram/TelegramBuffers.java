package ch.eiafr.enocean.telegram;

public class TelegramBuffers {
	
	private byte[] dataBuffer;
	private byte[] optionBuffer;

	public TelegramBuffers(byte[] dataBuffer, byte[] optionBuffer){
		this.dataBuffer = dataBuffer;
		this.optionBuffer = optionBuffer;
	}

	public byte[] getDataBuffer() {
		return dataBuffer;
	}

	public byte[] getOptionBuffer() {
		return optionBuffer;
	}

}
