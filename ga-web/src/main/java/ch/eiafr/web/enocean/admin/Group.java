package ch.eiafr.web.enocean.admin;

import java.util.Date;

public class Group {
	private int m_id;
	private String m_name;
	private String m_description;
	private String m_lastModifier;
	private Date m_lastModification;

	public Group() {
	}

	public Group(int p_id, String p_name, String p_description,
			String p_LastModifier, Date p_LastModification) {
		this.m_id = p_id;
		this.m_name = p_name;
		this.m_description = p_description;
		this.m_lastModifier = p_LastModifier;
		this.m_lastModification = p_LastModification;
	}

	public int getId() {
		return m_id;
	}

	public void setId(int m_id) {
		this.m_id = m_id;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String p_name) {
		this.m_name = p_name;
	}

	public String getDescription() {
		return m_description;
	}

	public void setDescription(String p_description) {
		this.m_description = p_description;
	}

	public String getLastModifier() {
		return m_lastModifier;
	}

	public void setLastModifier(String p_lastModifier) {
		this.m_lastModifier = p_lastModifier;
	}

	public Date getLastModification() {
		return m_lastModification;
	}

	public void setLastModification(Date p_lastModification) {
		this.m_lastModification = p_lastModification;
	}
}
