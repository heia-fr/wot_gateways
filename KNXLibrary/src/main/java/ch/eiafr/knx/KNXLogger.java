package ch.eiafr.knx;

import java.net.SocketException;
import java.util.Observable;

import ch.eiafr.knx.utils.KNXComm;

import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.process.ProcessEvent;

public class KNXLogger extends Observable implements IKNXLogger {

	private static KNXLogger m_Logger = null;
	private KNXComm m_comm = null;
	private static boolean m_ManualClose = false;
	
	private KNXLogger(){}
	
	public static KNXLogger getInstance(){
		if (m_Logger == null || m_ManualClose){
			m_Logger = new KNXLogger();
			m_ManualClose = false;
		}
		return m_Logger;
	}
	
	@Override
	public void initDatapointComm(String p_GatewayIP, String p_GatewayKNXAddr)
			throws KNXException, SocketException {
		m_comm = KNXComm.getInstance(p_GatewayIP, p_GatewayKNXAddr);
		m_comm.setLogger(this);
	}

	public void notifyListeners(ProcessEvent p_evt){
		setChanged();
		notifyObservers(p_evt);
	}
	
	@Override
	public void closeDatapointComm() {
		m_ManualClose = true;
		m_comm.close();	
	}
	
}
