package ch.eiafr.enocean;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.eiafr.enocean.eep.EEPExplorer;
import ch.eiafr.enocean.eep.EEPField;
import ch.eiafr.enocean.telegram.EEPTelegram;
import ch.eiafr.enocean.telegram.RadioTelegram;
import ch.eiafr.enocean.telegram.TelegramBuffers;
import ch.eiafr.enocean.thread.SerialComm;

/**
 * Entry point for communicating with the Enocean network
 * 
 * @author gb
 * 
 */
public class EnoceanCommunicator implements IEnoceanCommunicator {

	private static EnoceanCommunicator instance;
	private Thread serialCommThread;
	private SerialComm serialComm;
	private EEPExplorer eepExplorer;
	private ArrayList<EnoceanListener> listeners = new ArrayList<EnoceanListener>();

	/**
	 * Singleton for retrieving the instance
	 * 
	 * @param serialPort
	 *            The name of the serial port to use
	 * @param eepFile
	 *            The path to the EEP XML file
	 * @return The instance of the class
	 * @throws Exception
	 */
	public static IEnoceanCommunicator getInstance(String serialPort,
			String eepFile) throws Exception {
		if (instance == null)
			instance = new EnoceanCommunicator(serialPort, eepFile);
		return instance;
	}

	private EnoceanCommunicator(String serialPort, String eepFile)
			throws Exception {
		eepExplorer = EEPExplorer.getInstance(eepFile);
		serialComm = new SerialComm(serialPort, listeners);
		serialCommThread = new Thread(serialComm);
		serialCommThread.start();
	}

	/**
	 * Add a listener for incoming telegrams
	 * 
	 * @param listener
	 *            The listener to add
	 */
	public void addListener(EnoceanListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	/**
	 * Remove a listener for incoming telegrams
	 * 
	 * @param listener
	 *            The listener to remove
	 */
	public void removeListener(EnoceanListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	/**
	 * Close the serial port
	 */
	public void close() {
		serialCommThread.interrupt();
	}

	/**
	 * Send a telegram to the Enocean network
	 * 
	 * @param telegramBuffers
	 *            The payload data to send
	 * @param packetType
	 *            The type of packet
	 * @throws IOException
	 */
	public void send(TelegramBuffers telegramBuffers, int packetType)
			throws IOException {
		serialComm.send(telegramBuffers.getDataBuffer(),
				telegramBuffers.getOptionBuffer(), packetType);
	}

	/**
	 * Request the base Id of the sending dongle, response must be catched as
	 * event
	 * 
	 * @throws IOException
	 */
	public void requestBaseId() throws IOException {
		TelegramBuffers telegramBaseId = new TelegramBuffers(
				new byte[] { 0x08 }, null);
		send(telegramBaseId, SerialComm.COMMON_COMMAND);
	}

	/**
	 * Set the base Id of the sending dongle
	 * 
	 * @param baseId
	 *            The new base Id
	 * @throws IOException
	 */
	public void setBaseId(int baseId) throws IOException {
		byte baseId1 = (byte) ((baseId >> 24) & 0xFF);
		byte baseId2 = (byte) ((baseId >> 16) & 0xFF);
		byte baseId3 = (byte) ((baseId >> 8) & 0xFF);
		byte baseId4 = (byte) (baseId & 0xFF);
		TelegramBuffers telegramBaseId = new TelegramBuffers(new byte[] { 0x07,
				baseId1, baseId2, baseId3, baseId4 }, null);
		send(telegramBaseId, SerialComm.COMMON_COMMAND);
	}

	@Override
	public EEPTelegram decodeRadioTelegram(RadioTelegram radioTelegram,
			String RORG, String function, String type) {
		return eepExplorer.decodeRadioTelegram(radioTelegram, RORG, function,
				type);
	}

	@Override
	public Map<String, String> getAllRORG() {
		return eepExplorer.getAllRORG();
	}

	@Override
	public Map<String, String> getFunctionByRORG(String RORG) {
		return eepExplorer.getFunctionByRORG(RORG);
	}

	@Override
	public Map<String, String> getTypeByRORGAndFunction(String RORG,
			String function) {
		return eepExplorer.getTypeByRORGAndFunction(RORG, function);
	}

	@Override
	public Map<String, EEPField> getEEPFieldsInfo(String RORG, String function,
			String type, int direction) {
		return eepExplorer.getEEPFieldsInfo(RORG, function, type, direction);
	}

	@Override
	public EEPField getEEPFieldInfo(String RORG, String function, String type,
			String shortcut, int direction) {
		return eepExplorer.getEEPFieldInfo(RORG, function, type, shortcut, direction);
	}

	@Override
	public TelegramBuffers buildRadioDataBuffer(byte RORG, List<EEPField> data,
			int destinationId, int senderId) throws Exception {
		return eepExplorer.buildRadioDataBuffer(RORG, data, destinationId,
				senderId);
	}

	@Override
	public void sendLearn(int RORG, int function, int type, int manufacturer)
			throws IOException {
		int data = 0x80;
		function = function << 26;
		type = type << 19;
		manufacturer = manufacturer << 8;
		data |= function;
		data |= type;
		data |= manufacturer;
		ByteBuffer buff = ByteBuffer.allocate(4);
		buff.putInt(data);
		byte[] command = buff.array();
		byte[] dataBuff = new byte[10];
		dataBuff[0] = (byte) 0xA5;
		dataBuff[1] = command[0];
		dataBuff[2] = command[1];
		dataBuff[3] = command[2];
		dataBuff[4] = command[3];

		TelegramBuffers buffers = new TelegramBuffers(dataBuff, new byte[] {
				0x03, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, 0x00 });
		send(buffers, SerialComm.RADIO);
	}
}
