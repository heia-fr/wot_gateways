package ch.eiafr.web.enocean.admin;

public class Location {
	private int m_id;
	private String m_name;
	private String m_typeName;
	private String m_typeImgUrl;
	private String m_path;

	public Location() {
	}

	public Location(int p_id, String p_name, String p_typeName,
			String p_typeImgUrl, String p_path) {
		this.m_id = p_id;
		this.m_name = p_name;
		this.m_typeName = p_typeName;
		this.m_typeImgUrl = p_typeImgUrl;
		this.m_path = p_path;
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

	public String getTypeName() {
		return m_typeName;
	}

	public void setTypeName(String p_typeName) {
		this.m_typeName = p_typeName;
	}

	public String getTypeImgUrl() {
		return m_typeImgUrl;
	}

	public void setTypeImgUrl(String p_typeImgUrl) {
		this.m_typeImgUrl = p_typeImgUrl;
	}
	
	public String getPath() {
		return m_path;
	}

	public void setPath(String p_path) {
		this.m_path = p_path;
	}
}
