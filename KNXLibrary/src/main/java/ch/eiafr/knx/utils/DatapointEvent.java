package ch.eiafr.knx.utils;

import java.util.Date;

import tuwien.auto.calimero.datapoint.Datapoint;

public class DatapointEvent {

	private Datapoint m_Dp;
	private String m_Value;
	private String m_Url;
	private Date m_Date;
	
	public DatapointEvent(Datapoint p_dp, String p_value, String p_url){
		m_Dp = p_dp;
		m_Value = p_value;		
		m_Url = p_url;
		m_Date = new Date();
	}
	
	public Datapoint getDatapoint(){
		return m_Dp;
	}
	
	public String getValue(){
		return m_Value;
	}
	
	public String getUrl(){
		return m_Url;
	}
	
	public Date getDate(){
		return m_Date;
	}

}
