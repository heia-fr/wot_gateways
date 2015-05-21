package ch.eiafr.knx;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Map.Entry;

import org.jdom2.JDOMException;

import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.dptxlator.TranslatorTypes;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.process.ProcessEvent;
import ch.eiafr.knx.utils.ChildDescription;
import ch.eiafr.knx.utils.DatapointDescription;
import ch.eiafr.knx.utils.DatapointEvent;
import ch.eiafr.knx.utils.DatapointLocator;
import ch.eiafr.knx.utils.IPGateway;
import ch.eiafr.knx.utils.KNXComm;
import ch.eiafr.knx.utils.KNXDiscoverer;
import ch.eiafr.knx.utils.XMLGenerator;

public class KNXManagement extends Observable implements IKNXManagement {

	private static KNXManagement m_KNXManagementInstance = null;

	private DatapointLocator m_dpl = null;

	private KNXComm m_comm = null;

	private Map<String, Datapoint> m_Listening;

	/**
	 * Private constructor (Singleton pattern)
	 */
	private KNXManagement() {
		m_Listening = new HashMap<String, Datapoint>();
	}

	public static IKNXManagement getInstance() {
		if (m_KNXManagementInstance == null)
			m_KNXManagementInstance = new KNXManagement();
		return m_KNXManagementInstance;
	}

	@Override
	public ArrayList<IPGateway> discoverGateways(int p_SourcePort,
			int p_Duration) throws KNXException {
		return KNXDiscoverer.discoverGateways(p_SourcePort, p_Duration);
	}

	@Override
	public void generateXMLDatapoints(String p_ETSProjectFile,
			String p_TransformFile, String p_OutputFilePath) throws Exception {
		XMLGenerator.generateXMLDatapoints(p_ETSProjectFile, p_TransformFile,
				p_OutputFilePath);

	}
	
	@Override
	public void generateXMLDatapoints(String p_ETSProjectFile,
			InputStream p_TransformFile, String p_OutputFilePath) throws Exception {
		XMLGenerator.generateXMLDatapoints(p_ETSProjectFile, p_TransformFile,
				p_OutputFilePath);

	}

	@Override
	public void initDatapointLocator(String p_DatapointsXMLPath)
			throws JDOMException, IOException {
		m_dpl = new DatapointLocator(p_DatapointsXMLPath);
	}

	@Override
	public Datapoint findDatapoint(String p_functionality, String p_location,
			String p_action) throws Exception {
		if (m_dpl == null)
			throw new Exception("The datapoint locator was not initialized");
		return m_dpl.findDatapoint(p_functionality, p_location, p_action);
	}

	@Override
	public void initDatapointComm(String p_GatewayIP, String p_GatewayKNXAddr)
			throws KNXException, SocketException {
		m_comm = KNXComm.getInstance(p_GatewayIP, p_GatewayKNXAddr);
		m_comm.setManagement(this);
	}

	@Override
	public String readDatapoint(Datapoint p_dp) throws Exception {
		if (m_comm == null)
			throw new Exception(
					"The datapoint communicator was not initialized");
		return m_comm.readDatapoint(p_dp);
	}

	@Override
	public void writeDatapoint(Datapoint p_dp, String p_value) throws Exception {
		if (m_comm == null)
			throw new Exception(
					"The datapoint communicator was not initialized");
		m_comm.writeDatapoint(p_dp, p_value);
	}

	@Override
	public ArrayList<DatapointDescription> listDatapoints(
			String p_functionality, String p_location) throws Exception {
		if (m_dpl == null)
			throw new Exception("The datapoint locator was not initialized");
		return m_dpl.listDatapoints(p_functionality, p_location);
	}

	@Override
	public ArrayList<ChildDescription> listChildren(String p_location)
			throws Exception {
		if (m_dpl == null)
			throw new Exception("The datapoint locator was not initialized");
		return m_dpl.listChildren(p_location);
	}

	@Override
	public ArrayList<String> getAllUrls() throws Exception {
		if (m_dpl == null)
			throw new Exception("The datapoint locator was not initialized");
		return m_dpl.getAllUrls();
	}

	@Override
	public void addDatapointListener(Datapoint p_dp, String p_Url) {
		m_Listening.put(p_Url, p_dp);
	}

	@Override
	public void removeDatapointListener(String p_Url) {
		m_Listening.remove(p_Url);
	}

	public void notifyListeners(ProcessEvent p_evt) {
		Iterator<Entry<String, Datapoint>> l_it = m_Listening.entrySet()
				.iterator();
		while (l_it.hasNext()) {
			Map.Entry<String, Datapoint> l_entry = l_it.next();
			if (l_entry.getValue().getMainAddress().getRawAddress() == p_evt
					.getDestination().getRawAddress()) {
				DPTXlator l_translator;
				try {
					l_translator = TranslatorTypes.createTranslator(l_entry
							.getValue().getMainNumber(), l_entry.getValue()
							.getDPT());
					l_translator.setData(p_evt.getASDU());

					setChanged();
					notifyObservers(new DatapointEvent(l_entry.getValue(),
							l_translator.getValue(), l_entry.getKey()));
					break;
				} catch (KNXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void closeDatapointComm() {
		if (m_comm != null)
			m_comm.close();
	}

	@Override
	public String findUrlByGroup(int p_group) throws Exception {
		if (m_dpl == null)
			throw new Exception("The datapoint locator was not initialized");
		return m_dpl.findUrlByGroup(p_group);
	}

}
