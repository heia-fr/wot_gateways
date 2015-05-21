package ch.eiafr.enocean.thread;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import ch.eiafr.enocean.EnoceanListener;
import ch.eiafr.enocean.telegram.CommonCommandTelegram;
import ch.eiafr.enocean.telegram.EventTelegram;
import ch.eiafr.enocean.telegram.LearnTelegram;
import ch.eiafr.enocean.telegram.R_ORG;
import ch.eiafr.enocean.telegram.RadioTelegram;
import ch.eiafr.enocean.telegram.RemoteManagementTelegram;
import ch.eiafr.enocean.telegram.ResponseTelegram;
import ch.eiafr.enocean.telegram.SmartAckTelegram;

/**
 * Thread listening to the incoming telegrams on the serial port. Sending
 * outgoing telegrams
 * 
 * @author gb
 * 
 */
public class SerialComm implements Runnable {
	private static final int SER_INTERBYTE_TIMEOUT = 100;
	private static final int SER_SYNCH_CODE = 0x55;
	private static final int SER_HEADER_NR_BYTES = 4;
	public static final int RADIO = 1;
	public static final int RESPONSE = 2;
	public static final int EVENT = 4;
	public static final int COMMON_COMMAND = 5;
	public static final int SMART_ACK = 6;
	public static final int REMOTE_MAN = 7;

	private ArrayList<EnoceanListener> listeners;
	private CommPort port;
	private OutputStream output;
	private InputStream input;
	private int[] crcTable = { 0x00, 0x07, 0x0e, 0x09, 0x1c, 0x1b, 0x12, 0x15,
			0x38, 0x3f, 0x36, 0x31, 0x24, 0x23, 0x2a, 0x2d, 0x70, 0x77, 0x7e,
			0x79, 0x6c, 0x6b, 0x62, 0x65, 0x48, 0x4f, 0x46, 0x41, 0x54, 0x53,
			0x5a, 0x5d, 0xe0, 0xe7, 0xee, 0xe9, 0xfc, 0xfb, 0xf2, 0xf5, 0xd8,
			0xdf, 0xd6, 0xd1, 0xc4, 0xc3, 0xca, 0xcd, 0x90, 0x97, 0x9e, 0x99,
			0x8c, 0x8b, 0x82, 0x85, 0xa8, 0xaf, 0xa6, 0xa1, 0xb4, 0xb3, 0xba,
			0xbd, 0xc7, 0xc0, 0xc9, 0xce, 0xdb, 0xdc, 0xd5, 0xd2, 0xff, 0xf8,
			0xf1, 0xf6, 0xe3, 0xe4, 0xed, 0xea, 0xb7, 0xb0, 0xb9, 0xbe, 0xab,
			0xac, 0xa5, 0xa2, 0x8f, 0x88, 0x81, 0x86, 0x93, 0x94, 0x9d, 0x9a,
			0x27, 0x20, 0x29, 0x2e, 0x3b, 0x3c, 0x35, 0x32, 0x1f, 0x18, 0x11,
			0x16, 0x03, 0x04, 0x0d, 0x0a, 0x57, 0x50, 0x59, 0x5e, 0x4b, 0x4c,
			0x45, 0x42, 0x6f, 0x68, 0x61, 0x66, 0x73, 0x74, 0x7d, 0x7a, 0x89,
			0x8e, 0x87, 0x80, 0x95, 0x92, 0x9b, 0x9c, 0xb1, 0xb6, 0xbf, 0xb8,
			0xad, 0xaa, 0xa3, 0xa4, 0xf9, 0xfe, 0xf7, 0xf0, 0xe5, 0xe2, 0xeb,
			0xec, 0xc1, 0xc6, 0xcf, 0xc8, 0xdd, 0xda, 0xd3, 0xd4, 0x69, 0x6e,
			0x67, 0x60, 0x75, 0x72, 0x7b, 0x7c, 0x51, 0x56, 0x5f, 0x58, 0x4d,
			0x4a, 0x43, 0x44, 0x19, 0x1e, 0x17, 0x10, 0x05, 0x02, 0x0b, 0x0c,
			0x21, 0x26, 0x2f, 0x28, 0x3d, 0x3a, 0x33, 0x34, 0x4e, 0x49, 0x40,
			0x47, 0x52, 0x55, 0x5c, 0x5b, 0x76, 0x71, 0x78, 0x7f, 0x6A, 0x6d,
			0x64, 0x63, 0x3e, 0x39, 0x30, 0x37, 0x22, 0x25, 0x2c, 0x2b, 0x06,
			0x01, 0x08, 0x0f, 0x1a, 0x1d, 0x14, 0x13, 0xae, 0xa9, 0xa0, 0xa7,
			0xb2, 0xb5, 0xbc, 0xbb, 0x96, 0x91, 0x98, 0x9f, 0x8a, 0x8D, 0x84,
			0x83, 0xde, 0xd9, 0xd0, 0xd7, 0xc2, 0xc5, 0xcc, 0xcb, 0xe6, 0xe1,
			0xe8, 0xef, 0xfa, 0xfd, 0xf4, 0xf3 };

	/**
	 * Constructor
	 * 
	 * @param portName
	 *            The name of the serial port
	 * @param listeners
	 *            The listeners for incoming telegrams
	 * @throws Exception
	 */
	public SerialComm(String portName, ArrayList<EnoceanListener> listeners)
			throws Exception {
		this.listeners = listeners;
		CommPortIdentifier portIdentifier = CommPortIdentifier
				.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			throw new Exception("Port is currently in use");
		}

		CommPort commPort = portIdentifier
				.open(this.getClass().getName(), 2000); // timeout 2 s.
		if (!(commPort instanceof SerialPort)) {
			throw new Exception("Only serial port is supported");
		}
		port = commPort;

		SerialPort serialPort = (SerialPort) commPort;

		// 57600 bit/s, 8 bits, stop bit length 1, no parity bit
		serialPort.setSerialPortParams(57600, SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		input = serialPort.getInputStream();
		output = serialPort.getOutputStream();
	}

	/**
	 * Send a telegram to the Enocean network
	 * 
	 * @param data
	 *            Buffer of the data part of a frame
	 * @param optionalData
	 *            Buffer of the optional data part of a frame
	 * @param packetType
	 *            The type of packet
	 * @throws IOException
	 */
	public void send(byte[] data, byte[] optionalData, int packetType)
			throws IOException {
		int totalData = optionalData == null ? data.length : data.length
				+ optionalData.length;
		byte[] buffer = new byte[7 + totalData];
		buffer[0] = SER_SYNCH_CODE;
		buffer[1] = (byte) ((data.length & 0xFF00) >> 8);
		buffer[2] = (byte) (data.length & 0x00FF);
		buffer[3] = optionalData == null ? 0 : (byte) optionalData.length;
		buffer[4] = (byte) packetType;
		int crc = 0;
		for (int i = 1; i < 5; i++)
			crc = procCRC(crc, buffer[i] & 0xFF);
		buffer[5] = (byte) crc;
		// copy data
		for (int i = 0; i < data.length; i++)
			buffer[i + 6] = data[i];
		// copy optional
		if (optionalData != null)
			for (int i = 0; i < optionalData.length; i++)
				buffer[i + data.length + 6] = optionalData[i];
		crc = 0;
		for (int i = 0; i < totalData; i++)
			crc = procCRC(crc, buffer[i + 6] & 0xFF);
		buffer[6 + totalData] = (byte) crc;

		System.out.println("");
		for (int i = 0; i < buffer.length; i++)
			System.out.print(String.format("%02X ", buffer[i]));
		System.out.println("");

		output.write(buffer);
		output.flush();
		System.out.println("Sent at ms " + System.currentTimeMillis());
	}

	@Override
	public void run() {

		while (!Thread.currentThread().isInterrupted()) {
			RawTelegram telegram = getTelegram();
			if (telegram != null)
				decodeTelegram(telegram);
		}

		try {
			input.close();
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		port.close();
	}

	private void decodeTelegram(RawTelegram rawTelegram) {
		switch (rawTelegram.getType()) {
		case RADIO:
			RadioTelegram radioTelegram = new RadioTelegram(
					rawTelegram.getDataBuffer(), rawTelegram.getDataLength(),
					rawTelegram.getOptionLength());
			
			//discard RPS unassigned messages
			if(radioTelegram.getRORG() == R_ORG.RPS && (radioTelegram.getStatus() & 0x10) == 0)
				return;

			// check if learn telegram
			if ((radioTelegram.getRORG() == R_ORG.ONE_BS && (radioTelegram
					.getDataBuffer()[1] & 0x08) == 0)
					|| (radioTelegram.getRORG() == R_ORG.FOUR_BS && (radioTelegram
							.getDataBuffer()[4] & 0x08) == 0)) {
				LearnTelegram learnTelegram = new LearnTelegram(
						rawTelegram.getDataBuffer(),
						rawTelegram.getDataLength(),
						rawTelegram.getOptionLength());
				for (EnoceanListener listener : listeners)
					listener.learnTelegram(learnTelegram);
				break;
			}

			for (EnoceanListener listener : listeners)
				listener.radioTelegram(radioTelegram);

			break;

		case RESPONSE:
			ResponseTelegram responseTelegram = new ResponseTelegram(
					rawTelegram.getDataBuffer(), rawTelegram.getDataLength(),
					rawTelegram.getOptionLength());
			for (EnoceanListener listener : listeners)
				listener.responseTelegram(responseTelegram);
			break;

		case EVENT:
			EventTelegram eventTelegram = new EventTelegram(
					rawTelegram.getDataBuffer(), rawTelegram.getDataLength(),
					rawTelegram.getOptionLength());
			for (EnoceanListener listener : listeners)
				listener.eventTelegram(eventTelegram);
			break;

		case COMMON_COMMAND:
			// TODO implementation
			CommonCommandTelegram commonCommand = null;
			for (EnoceanListener listener : listeners)
				listener.commonCommandTelegram(commonCommand);
			break;

		case SMART_ACK:
			SmartAckTelegram smartAck = new SmartAckTelegram(
					rawTelegram.getDataBuffer(), rawTelegram.getDataLength(),
					rawTelegram.getOptionLength());
			for (EnoceanListener listener : listeners)
				listener.smartAckTelegram(smartAck);
			break;

		case REMOTE_MAN:
			RemoteManagementTelegram remoteManagement = new RemoteManagementTelegram(
					rawTelegram.getDataBuffer(), rawTelegram.getDataLength(),
					rawTelegram.getOptionLength());
			for (EnoceanListener listener : listeners)
				listener.remoteManagementTelegram(remoteManagement);
			break;
		}
	}

	private int procCRC(int crc, int data) {

		return crcTable[crc ^ data];
	}

	private RawTelegram getTelegram() {
		int rxByte = -1;
		int crc = 0;
		int count = 0;
		PacketStates state = PacketStates.GET_SYNC_STATE;
		long tickCount = 0;
		RawTelegram telegram = new RawTelegram();
		byte[] raw = telegram.getRawBuffer();

		// check for timeout between two bytes
		if (System.currentTimeMillis() - tickCount > SER_INTERBYTE_TIMEOUT)
			state = PacketStates.GET_SYNC_STATE;

		// state machine goes on
		try {
			while ((rxByte = input.read()) != -1) {
				tickCount = System.currentTimeMillis();

				switch (state) {
				// Waiting for packet sync byte 0x55
				case GET_SYNC_STATE:
					if (rxByte == SER_SYNCH_CODE) {
						state = PacketStates.GET_HEADER_STATE;
						count = 0;
						crc = 0;
						telegram = new RawTelegram();
						raw = telegram.getRawBuffer();
					}
					break;

				// Read the header bytes
				case GET_HEADER_STATE:
					raw[count++] = (byte) rxByte;
					crc = procCRC(crc, rxByte);

					if (count == SER_HEADER_NR_BYTES)
						state = PacketStates.CHECK_CRC8H_STATE;
					break;

				// Check header checksum & try to resynchonize if error
				// happened
				case CHECK_CRC8H_STATE:
					if (crc != rxByte) {
						// No. Check if there is a sync byte (0x55) in the
						// header
						int a = -1;
						for (int i = 0; i < SER_HEADER_NR_BYTES; i++)
							if (raw[i] == SER_SYNCH_CODE) {
								a = i + 1;
								break;
							}

						if ((a == -1) && (rxByte != SER_SYNCH_CODE)) {
							// Header and CRC8H does not contain the sync
							// code
							state = PacketStates.GET_SYNC_STATE;
							break;
						} else if ((a == -1) && (rxByte == SER_SYNCH_CODE)) {
							// Header does not have sync code but CRC8H
							// does.
							// The sync code could be the beginning of a
							// packet
							state = PacketStates.GET_HEADER_STATE;
							count = 0;
							crc = 0;
							break;
						}

						// Header has a sync byte. It could be a new
						// telegram.
						// Shift all bytes from the 0x55 code in the buffer.
						// Recalculate CRC8 for those bytes
						crc = 0;
						for (int i = 0; i < (SER_HEADER_NR_BYTES - a); i++) {
							raw[i] = raw[a + i];
							crc = procCRC(crc, raw[i]);
						}
						count = SER_HEADER_NR_BYTES - a;

						// Copy the just received byte to buffer
						raw[count++] = (byte) rxByte;
						crc = procCRC(crc, rxByte);

						if (count < SER_HEADER_NR_BYTES) {
							state = PacketStates.GET_HEADER_STATE;
							break;
						}
						break;
					}

					// CRC8H correct. Length fields values valid?
					if (telegram.getDataLength() + telegram.getOptionLength() == 0) {
						// No. Sync byte received?
						if (rxByte == SER_SYNCH_CODE) {
							// yes
							state = PacketStates.GET_HEADER_STATE;
							count = 0;
							crc = 0;
							break;
						}

						// Packet with correct CRC8H but wrong length
						// fields.
						state = PacketStates.GET_SYNC_STATE;
						return null;
					}

					// Correct header CRC8. Go to the reception of data.
					state = PacketStates.GET_DATA_STATE;
					count = 0;
					crc = 0;
					break;

				// Copy the information bytes
				case GET_DATA_STATE:
					// Copy byte in the packet buffer only if the received bytes
					// have enough room
					if (count < RawTelegram.BUFFER_SIZE) {
						telegram.getDataBuffer()[count] = (byte) rxByte;
						crc = procCRC(crc, rxByte);
					}

					// When all expected bytes received, go to calculate data
					// checksum
					if (++count == telegram.getDataLength()
							+ telegram.getOptionLength())
						state = PacketStates.CHECK_CRC8D_STATE;

					break;

				// Check the data CRC8
				case CHECK_CRC8D_STATE:
					// In all cases the state returns to the first state:
					// waiting for next sync byte
					state = PacketStates.GET_SYNC_STATE;

					// Enough space to allocate packet. Equals last byte the
					// calculated CRC8?
					if (crc == rxByte)
						return telegram;

					// False CRC8.
					// If the received byte equals sync code, then it could be
					// sync byte for next paquet.
					if (rxByte == SER_SYNCH_CODE) {
						state = PacketStates.GET_HEADER_STATE;
						count = 0;
						crc = 0;
					}

					return null;

				default:
					state = PacketStates.GET_SYNC_STATE;
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
