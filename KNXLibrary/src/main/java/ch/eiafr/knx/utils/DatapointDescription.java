package ch.eiafr.knx.utils;

public class DatapointDescription {
	
	private String m_Action;
	private String m_Id;
	private String m_Description;
	private String m_DatapointDescription;
	private int m_BitsSize;

	public DatapointDescription(String p_Action, String p_Id, String p_Description, String p_DatapointDescription, int p_BitsSize){
		m_Action = p_Action;
		m_Id = p_Id;
		m_Description = p_Description;
		m_DatapointDescription = p_DatapointDescription;
		m_BitsSize = p_BitsSize;
	}
	
	public String getAction(){
		return m_Action;
	}
	
	public String getId(){
		return m_Id;
	}
	
	public String getDescription(){
		return m_Description;
	}
	
	public String getDatapointDescription(){
		return m_DatapointDescription;
	}
	
	public int getBitsSize(){
		return m_BitsSize;
	}
	
}
