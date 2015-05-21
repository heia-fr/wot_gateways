package ch.eiafr.web.enocean;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ch.eiafr.enocean.EnoceanCommunicator;
import ch.eiafr.enocean.EnoceanListener;
import ch.eiafr.enocean.IEnoceanCommunicator;
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
import ch.eiafr.web.enocean.admin.EnOceanConfig;

public class EnOceanDispatcher implements EnoceanListener {

	private static EnOceanDispatcher enoceanDispatcher = null;
	private Map<String, Integer> m_Listening;
	private Map<LearnTelegram, Date> m_LearnTelegrams;
	private EnOceanStorage enoceanStorage;
	private IEnoceanCommunicator enoceanCommunicator;

	private EnOceanDispatcher() throws Exception {
		m_Listening = new HashMap<String, Integer>();
		m_LearnTelegrams = new HashMap<LearnTelegram, Date>();
		enoceanStorage = EnOceanStorage.getInstance();
		enoceanCommunicator = EnoceanCommunicator.getInstance(
				EnOceanConfig.getSerialPort(), EnOceanConfig.getEepFile());
	}

	public static EnOceanDispatcher getInstance() throws Exception {
		if (enoceanDispatcher == null)
			enoceanDispatcher = new EnOceanDispatcher();
		return enoceanDispatcher;
	}

	@Override
	public void radioTelegram(RadioTelegram radioTelegram) {
		// get eep number
		try {
			long sensorAddress = Long
					.parseLong(radioTelegram.getSenderID(), 16);
			SensorEEP eep = enoceanStorage.getEEP(sensorAddress);

			if (eep == null)
				return;

			// parse frame
			StringBuilder sb = new StringBuilder();
			sb.append(Integer.toHexString(eep.getRORG()));
			if (sb.length() < 2) {
				sb.insert(0, '0'); // pad with leading zero if needed
			}
			String RORG = sb.toString();

			sb = new StringBuilder();
			sb.append(Integer.toHexString(eep.getFunction()));
			if (sb.length() < 2) {
				sb.insert(0, '0'); // pad with leading zero if needed
			}
			String eep_function = sb.toString();

			sb = new StringBuilder();
			sb.append(Integer.toHexString(eep.getType()));
			if (sb.length() < 2) {
				sb.insert(0, '0'); // pad with leading zero if needed
			}
			String eep_type = sb.toString();

			EEPTelegram telegram = enoceanCommunicator.decodeRadioTelegram(
					radioTelegram, RORG, eep_function, eep_type);

			// get measures of device
			ArrayList<MeasureDescription> availableMeasures = enoceanStorage
					.listMeasures(eep.getIdSensor());

			for (String shortcut : telegram.getData().keySet()) {
				// insert into DB
				for (MeasureDescription measureDesc : availableMeasures) {
					if (measureDesc.getShortcut().equals(shortcut)) {
						int idData = enoceanStorage.addStorage(measureDesc
								.getIdMeasure(),
								telegram.getData().get(shortcut), new Date());

						// set last received data
						enoceanStorage.setLastMeasureData(
								measureDesc.getIdMeasure(), idData);

						// remove old data if no client
						enoceanStorage.deleteDataMeasure(measureDesc
								.getIdMeasure());

						// notify clients
						for (String urlMeasure : m_Listening.keySet()) {
							if (m_Listening.get(urlMeasure) == measureDesc
									.getIdMeasure()) {
								EnOceanClientNotifier.notifyClient(urlMeasure,
										telegram.getData().get(shortcut));
								break;
							}
						}
						break;
					}
				}
			}

			// hybrid mode ?
			if (!enoceanStorage.isHybridMode(sensorAddress)) {
				byte[] data = Arrays.copyOfRange(radioTelegram.getDataBuffer(),
						0, radioTelegram.getDataLength());
				byte[] optional = Arrays.copyOfRange(
						radioTelegram.getDataBuffer(),
						radioTelegram.getDataLength(),
						radioTelegram.getOptionLength()
								+ radioTelegram.getDataLength());
				TelegramBuffers buffer = new TelegramBuffers(data, optional);
				enoceanCommunicator.send(buffer, SerialComm.RADIO);
			}

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void responseTelegram(ResponseTelegram responseTelegram) {
		// TODO Auto-generated method stub

	}

	@Override
	public void eventTelegram(EventTelegram eventTelegram) {
		// TODO Auto-generated method stub

	}

	@Override
	public void commonCommandTelegram(
			CommonCommandTelegram commonCommandTelegram) {
		// TODO Auto-generated method stub

	}

	@Override
	public void smartAckTelegram(SmartAckTelegram smartAckTelegram) {
		// TODO Auto-generated method stub

	}

	@Override
	public void remoteManagementTelegram(
			RemoteManagementTelegram remoteManagementTelegram) {
		// TODO Auto-generated method stub

	}

	@Override
	public void learnTelegram(LearnTelegram learnTelegram) {
		m_LearnTelegrams.put(learnTelegram, new Date());
	}

	public void addMeasureListener(int idMeasure, String url) {
		m_Listening.put(url, idMeasure);
	}

	public void removeMeasureListener(String url) {
		m_Listening.remove(url);
	}

	public Map<LearnTelegram, Date> getDiscoveredDevices() {
		for (LearnTelegram learn : m_LearnTelegrams.keySet()) {
			if (new Date().getTime() - m_LearnTelegrams.get(learn).getTime() > 1000 * 60 * 30)
				m_LearnTelegrams.remove(learn);
		}

		return m_LearnTelegrams;
	}

}
