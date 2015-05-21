package ch.eiafr.enocean;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import ch.eiafr.enocean.eep.EEPField;
import ch.eiafr.enocean.telegram.EEPTelegram;
import ch.eiafr.enocean.telegram.RadioTelegram;
import ch.eiafr.enocean.telegram.TelegramBuffers;

public interface IEnoceanCommunicator {

	static final int TRANSMISSION = 1;
	static final int RECEPTION = 2;
	
	/**
	 * Add a listener for incoming telegrams
	 * 
	 * @param listener
	 *            The listener to add
	 */
	public void addListener(EnoceanListener listener);

	/**
	 * Remove a listener for incoming telegrams
	 * 
	 * @param listener
	 *            The listener to remove
	 */
	public void removeListener(EnoceanListener listener);

	/**
	 * Close the serial port
	 */
	public void close();

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
			throws IOException;

	/**
	 * Send a learn telegram to the Enocean network
	 * 
	 * @param RORG
	 *            The EEP RORG
	 * @param function
	 *            The EEP function
	 * @param type
	 *            The EEP type
	 * @param manufacturer
	 *            The manufacturer code
	 * @throws IOException
	 */
	public void sendLearn(int RORG, int function, int type, int manufacturer)
			throws IOException;

	/**
	 * Request the base Id of the sending dongle, response must be catched as
	 * event
	 * 
	 * @throws IOException
	 */
	public void requestBaseId() throws IOException;

	/**
	 * Set the base Id of the sending dongle
	 * 
	 * @param baseId
	 *            The new base Id
	 * @throws IOException
	 */
	public void setBaseId(int baseId) throws IOException;

	/**
	 * Decode a telegram according the the EEP number
	 * 
	 * @param radioTelegram
	 *            The telegram to decode
	 * @param RORG
	 *            The RORG number in hex representation (e.g. F6)
	 * @param function
	 *            The Function number in hex representation (e.g. 1F)
	 * @param type
	 *            The Type number in hex representation (e.g. 0A)
	 * @return The decoded telegram
	 */
	public EEPTelegram decodeRadioTelegram(RadioTelegram radioTelegram,
			String RORG, String function, String type);

	/**
	 * Get all RORG number and title from the EEP file
	 * 
	 * @return The RORGs with title
	 */
	public Map<String, String> getAllRORG();

	/**
	 * Get all Function number and title by RORG
	 * 
	 * @param RORG
	 *            The RORG number to find all sub functions, number in hex
	 *            representation (e.g. F6)
	 * @return The Functions with title
	 */
	public Map<String, String> getFunctionByRORG(String RORG);

	/**
	 * Get all Type number and title by RORG and Function
	 * 
	 * @param RORG
	 *            The RORG number to find all sub functions, number in hex
	 *            representation (e.g. F6)
	 * @param function
	 *            The function number to find all sub types, number in hex
	 *            representation (e.g. 0F)
	 * @return The Types with title
	 */
	public Map<String, String> getTypeByRORGAndFunction(String RORG,
			String function);

	/**
	 * Get all fields composing the corresponding EEP telegram
	 * 
	 * @param RORG
	 *            The RORG number in hex representation (e.g. F6)
	 * @param function
	 *            The Function number in hex representation (e.g. 1F)
	 * @param type
	 *            The Type number in hex representation (e.g. 0A)
	 * @return A Map of fields, Key is the shortcut and value is complete field
	 *         information
	 */
	public Map<String, EEPField> getEEPFieldsInfo(String RORG, String function,
			String type, int direction);

	/**
	 * Get a field composing the corresponding EEP telegram
	 * 
	 * @param RORG
	 *            The RORG number in hex representation (e.g. F6)
	 * @param function
	 *            The Function number in hex representation (e.g. 1F)
	 * @param type
	 *            The Type number in hex representation (e.g. 0A)
	 * @param shortcut
	 *            The shortcut of the field
	 * @return Complete field information
	 */
	public EEPField getEEPFieldInfo(String RORG, String function, String type,
			String shortcut, int direction);

	/**
	 * Build the data and optional data buffers according to the fields
	 * 
	 * @param RORG
	 *            RORG The RORG number
	 * @param data
	 *            The EEP fields to be included in the telegram
	 * @param destinationId
	 *            The destination Id
	 * @param senderId
	 *            The sender Id
	 * @return Data and optional data buffers
	 * @throws Exception
	 */
	public TelegramBuffers buildRadioDataBuffer(byte RORG, List<EEPField> data,
			int destinationId, int senderId) throws Exception;
}
