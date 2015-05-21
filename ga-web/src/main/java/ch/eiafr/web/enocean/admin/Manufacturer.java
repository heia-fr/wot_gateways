package ch.eiafr.web.enocean.admin;

public class Manufacturer {
	private int m_id;
	private String m_name;
	
	public Manufacturer() {
	}

	public Manufacturer(int p_id, String p_name) {
		this.m_id = p_id;
		this.m_name = p_name;
	}

	public int getId() {
		return m_id;
	}

	public void setId(int p_id) {
		this.m_id = p_id;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String p_name) {
		this.m_name = p_name;
	}
}
