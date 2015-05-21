package ch.eiafr.web.knx.admin;

import java.util.ArrayList;
import java.util.Date;

public class Datapoint {
	private String m_Name;
	private String m_Number;
	private Date m_LastRead;
	private ArrayList<Subscriber> m_Subscribers;
	
	public void setName(String p_Name){
		m_Name = p_Name;
	}
	
	public void setNumber(String p_Number){
		m_Number = p_Number;
	}
	
	public void setLastRead(Date p_LastRead){
		m_LastRead = p_LastRead;
	}
	
	public void setSubscribers(ArrayList<Subscriber> p_Subscribers){
		m_Subscribers = p_Subscribers;
	}
	
	public String getName(){
		return m_Name;
	}
	
	public String getNumber(){
		return m_Number;
	}
	
	public Date getLastRead(){
		return m_LastRead;
	}
	
	public ArrayList<Subscriber> getSubscribers(){
		return m_Subscribers;
	}
}
