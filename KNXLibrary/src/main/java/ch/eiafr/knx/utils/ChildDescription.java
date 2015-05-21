package ch.eiafr.knx.utils;

public class ChildDescription {

	private String m_Name;
	private boolean m_IsFunctionality;
	
	public ChildDescription(String p_Name, boolean p_IsFunctionality){
		m_Name = p_Name;
		m_IsFunctionality = p_IsFunctionality;
	}
	
	public String getName(){
		return m_Name;
	}

	
	public boolean isFunctionality(){
		return m_IsFunctionality;
	}

}
