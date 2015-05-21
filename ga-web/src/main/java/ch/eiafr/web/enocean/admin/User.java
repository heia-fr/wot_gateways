package ch.eiafr.web.enocean.admin;

public class User {
	private int m_id;
	private String m_username;
	private String m_password;
	private String m_firstName;
	private String m_lastName;
	private String m_email;
	private boolean m_admin;
	private boolean m_active;

	public User() {
	}

	public User(int p_id, String p_username, String p_password,
			String p_firstName, String p_lastName, String p_email,
			boolean p_admin, boolean p_active) {
		this.m_id = p_id;
		this.m_username = p_username;
		this.m_password = p_password;
		this.m_firstName = p_firstName;
		this.m_lastName = p_lastName;
		this.m_email = p_email;
		this.m_admin = p_admin;
		this.m_active = p_active;
	}
	
	public User(int p_id, String p_password,
			String p_firstName, String p_lastName, String p_email,
			boolean p_admin, boolean p_active) {
		this.m_id = p_id;
		this.m_password = p_password;
		this.m_firstName = p_firstName;
		this.m_lastName = p_lastName;
		this.m_email = p_email;
		this.m_admin = p_admin;
		this.m_active = p_active;
	}

	public int getId() {
		return m_id;
	}

	public void setId(int p_id) {
		this.m_id = p_id;
	}

	public String getUsername() {
		return m_username;
	}

	public void setUsername(String p_username) {
		this.m_username = p_username;
	}

	public String getPassword() {
		return m_password;
	}

	public void setPassword(String p_password) {
		this.m_password = p_password;
	}

	public String getFirstName() {
		return m_firstName;
	}

	public void setFirstName(String p_firstName) {
		this.m_firstName = p_firstName;
	}

	public String getLastName() {
		return m_lastName;
	}

	public void setLastName(String p_lastName) {
		this.m_lastName = p_lastName;
	}

	public String getEmail() {
		return m_email;
	}

	public void setEmail(String p_email) {
		this.m_email = p_email;
	}

	public boolean isAdmin() {
		return m_admin;
	}

	public void setAdmin(boolean p_admin) {
		this.m_admin = p_admin;
	}
	
	public boolean isActive() {
		return m_active;
	}

	public void setActive(boolean p_active) {
		this.m_active = p_active;
	}
}