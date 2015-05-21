import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.eiafr.enocean.EnoceanCommunicator;
import ch.eiafr.enocean.EnoceanListener;
import ch.eiafr.enocean.IEnoceanCommunicator;
import ch.eiafr.enocean.eep.EEPExplorer;
import ch.eiafr.enocean.eep.EEPField;
import ch.eiafr.enocean.telegram.CommonCommandTelegram;
import ch.eiafr.enocean.telegram.EEPTelegram;
import ch.eiafr.enocean.telegram.EventTelegram;
import ch.eiafr.enocean.telegram.LearnTelegram;
import ch.eiafr.enocean.telegram.RadioTelegram;
import ch.eiafr.enocean.telegram.RemoteManagementTelegram;
import ch.eiafr.enocean.telegram.ResponseTelegram;
import ch.eiafr.enocean.telegram.SmartAckTelegram;
import ch.eiafr.enocean.telegram.TelegramBuffers;
import ch.eiafr.enocean.thread.SerialComm;

public class Test implements EnoceanListener {

	/**
	 * @param args
	 */
	private static HashMap<String, String[]> knownDevices = new HashMap<String, String[]>();
	private static IEnoceanCommunicator eoc;

	public static void main(String[] args) {

		knownDevices.put("0101EF16", new String[] { "A5", "04", "01" });
		knownDevices.put("008621F5", new String[] { "D5", "00", "01" });
		try {
			eoc = EnoceanCommunicator.getInstance(
					"/dev/tty.usbserial-FTVQTFH9",
					"/Users/gb/Documents/eep2.5.xml");

			Map<String, EEPField> fieldsT = eoc.getEEPFieldsInfo("F6", "02",
					"03", IEnoceanCommunicator.TRANSMISSION);
			RadioTelegram rt = eoc.decodeRadioTelegram(new RadioTelegram(new byte[] {(byte) 0xF6, 0x00, 0x00, 0x25, (byte) 0xCA, (byte) 0x04, 0x20}, 7, 0), "F6", "02", "01");
			eoc.close();
			eoc.addListener(new Test());
			TelegramBuffers buffersRaw = new TelegramBuffers(new byte[] {
					(byte) 0xf6, 0x30, 0x00, (byte) 0x8b, 0x26, (byte) 0x88,
					0x30 }, new byte[] { 0x03, (byte) 0xff, (byte) 0xff,
					(byte) 0xff, (byte) 0xff, (byte) 0xff, 0x00 });
			// eoc.send(buffersRaw, SerialComm.RADIO);
			Map<String, EEPField> fields = eoc.getEEPFieldsInfo("A5", "20",
					"01", IEnoceanCommunicator.RECEPTION);
			fields.get("R1").setValue(1);
			fields.get("EB").setValue(1);
			fields.get("NU").setValue(1);
			fields.get("T21").setValue(1);
			List<EEPField> data = new ArrayList<EEPField>(fields.values());
			/*
			 * TelegramBuffers buffers =
			 * eepExplorer.buildRadioDataBuffer((byte)0xF6, data, 0xFFFFFFFF,
			 * 0x008b2688); eoc.send(buffers, SerialComm.RADIO);
			 */
			TelegramBuffers telegramBaseId = new TelegramBuffers(
					new byte[] { 0x08 }, null);
			eoc.send(telegramBaseId, SerialComm.COMMON_COMMAND);
			Thread.sleep(220);
			// eoc.send(new byte[]{(byte) 0xf6, 0x00, 0x00, (byte) 0x8b, 0x26,
			// (byte) 0x88, 0x20}, new byte[]{0x03, (byte) 0xff, (byte) 0xff,
			// (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x00}, SerialComm.RADIO);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * try { EEPExplorer eepExplorer =
		 * EEPExplorer.getInstance("/Users/gb/Documents/eep2.5.xml");
		 * eepExplorer.getAllRORG(); eepExplorer.getFunctionByRORG("A5");
		 * eepExplorer.getTypeByRORGAndFunction("A5", "02"); } catch
		 * (JDOMException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); }
		 */

	}

	public Test() {
	}

	@Override
	public void radioTelegram(RadioTelegram radioTelegram) {
		System.out.println(new Date() + " Radio\tsender: "
				+ radioTelegram.getSenderID());
		StringBuilder sb = new StringBuilder("\t");
		for (int i = 0; i < radioTelegram.getDataLength(); i++) {
			sb.append(String.format("%02X ", radioTelegram.getDataBuffer()[i]));
		}
		System.out.println(sb.toString());
		if (knownDevices.containsKey(radioTelegram.getSenderID())) {
			String[] EEP = knownDevices.get(radioTelegram.getSenderID());
			// System.out.println("Decode RADIO telegram: " + EEP[0] + " " +
			// EEP[1] + " " + EEP[2]);
			EEPTelegram eepTelegram = eoc.decodeRadioTelegram(radioTelegram,
					EEP[0], EEP[1], EEP[2]);
			Map<String, EEPField> fields = eoc.getEEPFieldsInfo(EEP[0], EEP[1],
					EEP[2], IEnoceanCommunicator.TRANSMISSION);
			String valueDescritpion = "", fieldData = "";
			for (String key : eepTelegram.getData().keySet()) {
				if (fields.get(key) != null) {
					valueDescritpion = "";
					fieldData = fields.get(key).getData();
					if (fields.get(key).hasPossibleValues())
						valueDescritpion = fields.get(key).getPossibleValues()
								.get(eepTelegram.getData().get(key));
				}
				System.out.println("\t" + fieldData + " (" + key + ") "
						+ eepTelegram.getData().get(key) + " "
						+ valueDescritpion);
			}
		}
	}

	@Override
	public void responseTelegram(ResponseTelegram responseTelegram) {
		System.out.println("Response " + responseTelegram.getReturnCode());
	}

	@Override
	public void eventTelegram(EventTelegram eventTelegram) {
		System.out.println("Event");

	}

	@Override
	public void commonCommandTelegram(
			CommonCommandTelegram commonCommandTelegram) {
		System.out.println("Common command");

	}

	@Override
	public void smartAckTelegram(SmartAckTelegram smartAckTelegram) {
		System.out.println("Smart ack");

	}

	@Override
	public void remoteManagementTelegram(
			RemoteManagementTelegram remoteManagementTelegram) {
		// TODO Auto-generated method stub

	}

	@Override
	public void learnTelegram(LearnTelegram learnTelegram) {
		System.out.println("Learn\tsender: " + learnTelegram.getSenderID());
		String RORG = String.format("%02X ", learnTelegram.getRORGNumber());
		String function = String.format("%02X ", learnTelegram.getFunction());
		String type = String.format("%02X ", learnTelegram.getType());
		System.out.println("\tRORG: " + RORG + " Function: " + function
				+ " Type: " + type);
		StringBuilder sb = new StringBuilder("\t");
		for (int i = 0; i < learnTelegram.getDataLength(); i++) {
			sb.append(String.format("%02X ", learnTelegram.getDataBuffer()[i]));
		}
		System.out.println(sb.toString());
		if (!knownDevices.containsKey(learnTelegram.getSenderID())) {
			knownDevices.put(learnTelegram.getSenderID(), new String[] { RORG,
					function, type });
			System.out.println("\tDevice added to known list");
		}
	}

}
