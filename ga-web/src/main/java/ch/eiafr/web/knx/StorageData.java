package ch.eiafr.web.knx;

import java.util.Date;

public class StorageData {
	private String m_Value;
	private Date m_Date;
	
	public StorageData(String p_Value, Date p_Date){
		m_Value = p_Value;
		m_Date = p_Date;
	}
	
	public String getValue(){
		return m_Value;
	}
	
	public Date getDate(){
		return m_Date;
	}
}
