package ch.eiafr.enocean.telegram;

import java.util.Map;


/**
 * Represents a Common command telegram, see Enocean EEP description for more information
 * @author gb
 *
 */
public class CommonCommandTelegram {

	private CommonCommandCode commandCode;
	private Map<String, Integer> data;
	private Map<String, Integer> optionalData;
}
