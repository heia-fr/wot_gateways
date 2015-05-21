package ch.eiafr.web.knx.admin;

public class Subscriber{
	private String m_Referer;
	private int m_Days;
	
	public Subscriber(String p_Referer, int p_Days){
		m_Referer = p_Referer;
		m_Days = p_Days;
	}
	
	public String getReferer(){
		return m_Referer;
	}
	
	public int getDays(){
		return m_Days;
	}
	
}
