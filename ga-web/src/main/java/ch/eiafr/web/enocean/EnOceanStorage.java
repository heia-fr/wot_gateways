package ch.eiafr.web.enocean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eiafr.enocean.EnoceanCommunicator;
import ch.eiafr.enocean.IEnoceanCommunicator;
import ch.eiafr.enocean.eep.EEPField;
import ch.eiafr.web.enocean.admin.Data;
import ch.eiafr.web.enocean.admin.EnOceanConfig;
import ch.eiafr.web.enocean.admin.Group;
import ch.eiafr.web.enocean.admin.Location;
import ch.eiafr.web.enocean.admin.Manufacturer;
import ch.eiafr.web.enocean.admin.Measure;
import ch.eiafr.web.enocean.admin.Sensor;
import ch.eiafr.web.enocean.admin.User;

public class EnOceanStorage {
	/**
	 * Manage the storage of data in the database
	 * 
	 * @author gb
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(EnOceanStorage.class);
	private static EnOceanStorage m_EnOceanStorage = new EnOceanStorage();
	private Connection m_db = null;

	private EnOceanStorage() {
	}

	public static EnOceanStorage getInstance() {
		return m_EnOceanStorage;
	}

	/**
	 * Open the connection to the database
	 * 
	 * @param p_Driver
	 *            The driver to load
	 * @param p_User
	 *            Username
	 * @param p_Password
	 *            Password
	 * @param p_Database
	 *            Database url
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public void openConnection(String p_Driver, String p_User,
			String p_Password, String p_Database)
			throws ClassNotFoundException, SQLException {
		Class.forName(p_Driver);
		m_db = DriverManager.getConnection(p_Database, p_User, p_Password);

	}

	/**
	 * Close the connection to the database
	 */
	public void closeConnection() {
		try {
			m_db.close();
		} catch (SQLException e) {
			logger.error("Error when closing database");
		}
	}

	/**
	 * Get all the registered users
	 * 
	 * @return ArrayList of user object
	 * @throws SQLException
	 */
	public ArrayList<User> getUsers() throws SQLException {
		ArrayList<User> l_users = new ArrayList<User>();

		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT pk_user, usr_username, usr_password, usr_first_name, usr_last_name, usr_email, usr_is_admin, usr_is_active FROM user");
		ResultSet l_rs = l_pstmt.executeQuery();

		while (l_rs.next()) {
			User l_user = new User(l_rs.getInt(1), l_rs.getString(2),
					l_rs.getString(3), l_rs.getString(4), l_rs.getString(5),
					l_rs.getString(6), l_rs.getBoolean(7), l_rs.getBoolean(8));
			l_users.add(l_user);
		}

		l_rs.close();
		l_pstmt.close();
		return l_users;
	}

	/**
	 * Get the specified user
	 * 
	 * @param p_id
	 *            The user id
	 * @return User object
	 * @throws SQLException
	 */
	public User getUserFromId(int p_id) throws SQLException {
		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT pk_user, usr_username, usr_password, usr_first_name, usr_last_name, usr_email, usr_is_admin, usr_is_active FROM user WHERE pk_user = ?");
		l_pstmt.setInt(1, p_id);
		ResultSet l_rs = l_pstmt.executeQuery();

		User l_user = null;
		if (l_rs.next()) {
			l_user = new User(l_rs.getInt(1), l_rs.getString(2),
					l_rs.getString(3), l_rs.getString(4), l_rs.getString(5),
					l_rs.getString(6), l_rs.getBoolean(7), l_rs.getBoolean(8));
		}

		l_rs.close();
		l_pstmt.close();
		return l_user;
	}

	/**
	 * Create a new user
	 * 
	 * @param p_user
	 *            The user object
	 * @throws SQLException
	 */
	public void addUser(User p_user) throws SQLException {
		PreparedStatement l_pstmt = m_db
				.prepareStatement("INSERT INTO user(usr_username, usr_password, usr_first_name, usr_last_name, usr_email, usr_is_admin) VALUES (?, ?, ?, ?, ?, ?)");
		l_pstmt.setString(1, p_user.getUsername());
		l_pstmt.setString(2, p_user.getPassword());
		l_pstmt.setString(3, p_user.getFirstName());
		l_pstmt.setString(4, p_user.getLastName());
		l_pstmt.setString(5, p_user.getEmail());
		l_pstmt.setBoolean(6, p_user.isAdmin());
		l_pstmt.executeUpdate();
		l_pstmt.close();
	}

	/**
	 * Update the specified user
	 * 
	 * @param p_user
	 *            The user object
	 * @param p_id
	 *            The user id
	 * @throws SQLException
	 */
	public void updateUser(User p_user, int p_id) throws SQLException {
		PreparedStatement l_pstmt = m_db
				.prepareStatement("UPDATE user SET usr_username = ?, usr_password = ?, usr_first_name = ?, usr_last_name = ?, usr_email = ?, usr_is_admin = ?, usr_is_active = ? WHERE pk_user = ?");
		l_pstmt.setString(1, p_user.getUsername());
		l_pstmt.setString(2, p_user.getPassword());
		l_pstmt.setString(3, p_user.getFirstName());
		l_pstmt.setString(4, p_user.getLastName());
		l_pstmt.setString(5, p_user.getEmail());
		l_pstmt.setBoolean(6, p_user.isAdmin());
		l_pstmt.setBoolean(7, p_user.isActive());
		l_pstmt.setInt(8, p_id);
		l_pstmt.executeUpdate();
		l_pstmt.close();
	}

	/**
	 * Delete the specified user
	 * 
	 * @param p_id
	 *            The user id
	 * @throws SQLException
	 */
	public void deleteUser(int p_id) throws SQLException {
		PreparedStatement l_pstmt = m_db
				.prepareStatement("UPDATE user SET usr_is_active = ? WHERE pk_user = ?");
		l_pstmt.setInt(1, 0);
		l_pstmt.setInt(2, p_id);
		l_pstmt.executeUpdate();
		l_pstmt.close();
	}

	/**
	 * Get all the groups
	 * 
	 * @return ArrayList of group objects
	 * @throws SQLException
	 */
	public ArrayList<Group> getGroups() throws SQLException {
		ArrayList<Group> l_groups = new ArrayList<Group>();

		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT pk_group, gr_name, gr_description, usr_username, gr_date_of_modification FROM `group` INNER JOIN user ON pk_user = gr_fk_user_mod");
		ResultSet l_rs = l_pstmt.executeQuery();

		while (l_rs.next()) {
			Group l_group = new Group(l_rs.getInt(1), l_rs.getString(2),
					l_rs.getString(3), l_rs.getString(4), l_rs.getDate(5));
			l_groups.add(l_group);
		}

		l_rs.close();
		l_pstmt.close();
		return l_groups;
	}

	/**
	 * Get the specified group
	 * 
	 * @param p_id
	 *            The group id
	 * @return Group object
	 * @throws SQLException
	 */
	public Group getGroupFromId(int p_id) throws SQLException {
		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT pk_group, gr_name, gr_description, usr_username, gr_date_of_modification FROM `group` INNER JOIN user ON pk_user = gr_fk_user_mod WHERE pk_group = ?");
		l_pstmt.setInt(1, p_id);
		ResultSet l_rs = l_pstmt.executeQuery();

		Group l_group = null;
		if (l_rs.next()) {
			l_group = new Group(l_rs.getInt(1), l_rs.getString(2),
					l_rs.getString(3), l_rs.getString(4), l_rs.getDate(5));
		}

		l_rs.close();
		l_pstmt.close();
		return l_group;
	}

	/**
	 * Create a new group
	 * 
	 * @param p_group
	 *            The group object
	 * @throws SQLException
	 */
	public void addGroup(Group p_group) throws SQLException {
		// get the pk_user of the connected user (from username)
		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT pk_user FROM user WHERE usr_username = ?");
		l_pstmt.setString(1, p_group.getLastModifier());
		ResultSet l_rs = l_pstmt.executeQuery();

		int l_pk_user = 0;
		if (l_rs.next()) {
			l_pk_user = l_rs.getInt(1);
		}
		l_rs.close();
		l_pstmt.close();
		
		// insert the group into the database
		l_pstmt = m_db
				.prepareStatement("INSERT INTO `group`(gr_name, gr_description, gr_fk_user_mod) VALUES (?, ?, ?)");
		l_pstmt.setString(1, p_group.getName());
		l_pstmt.setString(2, p_group.getDescription());
		l_pstmt.setInt(3, l_pk_user);
		l_pstmt.executeUpdate();
		l_pstmt.close();
	}

	/**
	 * Update the specified group
	 * 
	 * @param p_group
	 *            The group object
	 * @param p_id
	 *            The group id
	 * @throws SQLException
	 */
	public void updateGroup(Group p_group, int p_id) throws SQLException {
		// get the pk_user of the connected user (from username)
		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT pk_user FROM user WHERE usr_username = ?");
		l_pstmt.setString(1, p_group.getLastModifier());
		ResultSet l_rs = l_pstmt.executeQuery();

		int l_pk_user = 0;
		if (l_rs.next()) {
			l_pk_user = l_rs.getInt(1);
		}
		l_rs.close();
		l_pstmt.close();

		// update the group into the database
		l_pstmt = m_db
				.prepareStatement("UPDATE `group` SET gr_name = ?, gr_description = ?, gr_fk_user_mod = ? WHERE pk_group = ?");
		l_pstmt.setString(1, p_group.getName());
		l_pstmt.setString(2, p_group.getDescription());
		l_pstmt.setInt(3, l_pk_user);
		l_pstmt.setInt(4, p_id);
		l_pstmt.executeUpdate();
		l_pstmt.close();
	}

	/**
	 * Delete the specified group
	 * 
	 * @param p_id
	 *            The group id
	 * @throws SQLException
	 */
	public void deleteGroup(int p_id) throws SQLException {
		// get the fk_sensor(s) of the group (into group_sensor)
		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT fk_sensor FROM group_sensor WHERE fk_group = ?");
		l_pstmt.setInt(1, p_id);
		ResultSet l_rs = l_pstmt.executeQuery();

		ArrayList<Integer> l_fk_sensors = new ArrayList<Integer>();
		while (l_rs.next()) {
			l_fk_sensors.add(l_rs.getInt(1));
		}
		l_rs.close();
		l_pstmt.close();

		// delete the connection(s) between the group and the sensor(s)
		for (int i = 0; i < l_fk_sensors.size(); i++) {
			l_pstmt = m_db
					.prepareStatement("DELETE FROM group_sensor WHERE fk_group = ? AND fk_sensor = ?");
			l_pstmt.setInt(1, p_id);
			l_pstmt.setInt(2, l_fk_sensors.get(i));
			l_pstmt.executeUpdate();
			l_pstmt.close();

			// verify if the sensor belong to an another group (if not, it's
			// removed)
			l_pstmt = m_db
					.prepareStatement("SELECT COUNT(fk_group) FROM group_sensor WHERE fk_sensor = ?");
			l_pstmt.setInt(1, l_fk_sensors.get(i));
			l_rs = l_pstmt.executeQuery();

			int connection = 1;
			if (l_rs.next()) {
				connection = l_rs.getInt(1);
			}
			l_rs.close();
			l_pstmt.close();

			// if the number of connections is equal to 0, we remove the sensor
			// (it belongs anymore to a group)
			if (connection == 0) {
				ArrayList<Integer> l_pk_measures = new ArrayList<Integer>();

				l_pstmt = m_db
						.prepareStatement("SELECT pk_measure FROM measure WHERE mea_fk_sensor = ?");
				l_pstmt.setInt(1, l_fk_sensors.get(i));
				l_rs = l_pstmt.executeQuery();

				while (l_rs.next()) {
					l_pk_measures.add(l_rs.getInt(1));
				}
				l_rs.close();
				l_pstmt.close();

				// delete the data of the measure(s)
				for (int j = 0; j < l_pk_measures.size(); j++) {
					// verify if a client exists for this measure
					l_pstmt = m_db
							.prepareStatement("SELECT COUNT(pk_client) FROM storage_client WHERE scl_fk_measure = ?");
					l_pstmt.setInt(1, l_pk_measures.get(j));
					l_rs = l_pstmt.executeQuery();

					int client = 0;
					if (l_rs.next()) {
						client = l_rs.getInt(1);
					}
					l_rs.close();
					l_pstmt.close();

					if (client != 0) {
						l_pstmt = m_db
								.prepareStatement("DELETE FROM storage_client WHERE scl_fk_measure = ?");
						l_pstmt.setInt(1, l_pk_measures.get(j));
						l_pstmt.executeUpdate();
						l_pstmt.close();
					}

					// update the measure to null
					l_pstmt = m_db
							.prepareStatement("UPDATE measure SET mea_fk_data_sensor = ? WHERE pk_measure = ?");
					l_pstmt.setInt(1, l_pk_measures.get(j));
					l_pstmt.executeUpdate();
					l_pstmt.close();

					l_pstmt = m_db
							.prepareStatement("DELETE FROM data_sensor WHERE data_fk_measure = ?");
					l_pstmt.setInt(1, l_pk_measures.get(j));
					l_pstmt.executeUpdate();
					l_pstmt.close();

					// delete the measure
					l_pstmt = m_db
							.prepareStatement("DELETE FROM measure WHERE pk_measure = ?");
					l_pstmt.setInt(1, l_pk_measures.get(j));
					l_pstmt.executeUpdate();
					l_pstmt.close();
				}

				// delete the location (if it doesn't belong to another sensor /
				// has child(s))
				l_pstmt = m_db
						.prepareStatement("SELECT sens_fk_location FROM sensor WHERE pk_sensor = ?");
				l_pstmt.setInt(1, l_fk_sensors.get(i));
				l_rs = l_pstmt.executeQuery();

				int l_locationId = 0;
				if (l_rs.next()) {
					l_locationId = l_rs.getInt(1);
				}
				l_rs.close();
				l_pstmt.close();

				// delete the sensor
				l_pstmt = m_db
						.prepareStatement("DELETE FROM sensor WHERE pk_sensor = ?");
				l_pstmt.setInt(1, l_fk_sensors.get(i));
				l_pstmt.executeUpdate();
				l_pstmt.close();

				deleteLocation(l_locationId);
			}
		}

		// delete the specified group
		l_pstmt = m_db
				.prepareStatement("DELETE FROM `group` WHERE pk_group = ?");
		l_pstmt.setInt(1, p_id);
		l_pstmt.executeUpdate();
		l_pstmt.close();
	}

	/**
	 * Get all the registered sensors
	 * 
	 * @return JSON array : pk_sensor, sens_name, sens_description, pk_location,
	 *         loc_path, loc_type_name, loc_type_img_url, man_name,
	 *         sens_eep_rorg, sens_eep_function, sens_eep_type,
	 *         sens_date_of_modification, usr_username, sens_is_actuator,
	 *         sens_is_in_hybrid_mode, sens_address & measure
	 * @throws SQLException
	 */
	public ArrayList<Sensor> getSensors() throws SQLException {
		ArrayList<Sensor> l_sensors = new ArrayList<Sensor>();

		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT DISTINCT pk_sensor, sens_name, sens_description, pk_location, loc_path, loc_type_name, loc_type_img_url, man_name, sens_eep_rorg, sens_eep_function, sens_eep_type, usr_username, sens_date_of_modification, sens_is_actuator, sens_is_in_hybrid_mode, sens_address FROM sensor INNER JOIN user ON pk_user = sens_fk_user_mod INNER JOIN location ON pk_location = sens_fk_location INNER JOIN location_type ON pk_location_type = loc_fk_type INNER JOIN manufacturer ON pk_manufacturer = sens_fk_manufacturer INNER JOIN group_sensor ON fk_sensor = pk_sensor");
		ResultSet l_rs = l_pstmt.executeQuery();

		while (l_rs.next()) {
			Sensor l_sensor = new Sensor(l_rs.getInt(1), l_rs.getString(2),
					l_rs.getString(3), l_rs.getInt(4), l_rs.getString(5),
					l_rs.getString(6), l_rs.getString(7), l_rs.getString(8),
					l_rs.getInt(9), l_rs.getInt(10), l_rs.getInt(11),
					l_rs.getString(12), l_rs.getDate(13), l_rs.getBoolean(14),
					l_rs.getBoolean(15), l_rs.getInt(16));
			l_sensors.add(l_sensor);
		}
		l_rs.close();
		l_pstmt.close();

		// get the measure(s) of each sensor
		for (int i = 0; i < l_sensors.size(); i++) {
			ArrayList<Measure> l_measures = new ArrayList<Measure>();
			l_pstmt = m_db
					.prepareStatement("SELECT mea_unit, mea_scale_max, mea_scale_min, mea_eep_shortcut, pk_measure FROM measure WHERE mea_fk_sensor = ?");
			l_pstmt.setInt(1, l_sensors.get(i).getId());
			l_rs = l_pstmt.executeQuery();

			while (l_rs.next()) {
				Measure l_measure = new Measure(l_rs.getInt(5),
						l_rs.getString(1), l_rs.getFloat(2), l_rs.getFloat(3),
						l_rs.getString(4));
				l_measures.add(l_measure);
			}
			l_rs.close();
			l_pstmt.close();
			l_sensors.get(i).setMeasure(l_measures);
		}
		return l_sensors;
	}

	/**
	 * Get all the registered sensors from the specified group
	 * 
	 * @param p_groupId
	 *            The group id
	 * @return JSON array : pk_sensor, sens_name, sens_description, pk_location,
	 *         loc_path, loc_type_name, loc_type_img_url, man_name,
	 *         sens_eep_rorg, sens_eep_function, sens_eep_type,
	 *         sens_date_of_modification, usr_username, sens_is_actuator,
	 *         sens_is_in_hybrid_mode, sens_address & measure
	 * @throws SQLException
	 */
	public ArrayList<Sensor> getSensorsFromGroupId(int p_groupId)
			throws SQLException {
		ArrayList<Sensor> l_sensors = new ArrayList<Sensor>();

		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT DISTINCT pk_sensor, sens_name, sens_description, pk_location, CONCAT(loc_name, '.', loc_path), loc_type_name, loc_type_img_url, man_name, sens_eep_rorg, sens_eep_function, sens_eep_type, usr_username, sens_date_of_modification, sens_is_actuator, sens_is_in_hybrid_mode, sens_address FROM sensor INNER JOIN user ON pk_user = sens_fk_user_mod INNER JOIN location ON pk_location = sens_fk_location INNER JOIN location_type ON pk_location_type = loc_fk_type INNER JOIN manufacturer ON pk_manufacturer = sens_fk_manufacturer INNER JOIN group_sensor ON fk_sensor = pk_sensor WHERE fk_group = ?");
		l_pstmt.setInt(1, p_groupId);
		ResultSet l_rs = l_pstmt.executeQuery();

		while (l_rs.next()) {
			Sensor l_sensor = new Sensor(l_rs.getInt(1), l_rs.getString(2),
					l_rs.getString(3), l_rs.getInt(4), l_rs.getString(5),
					l_rs.getString(6), l_rs.getString(7), l_rs.getString(8),
					l_rs.getInt(9), l_rs.getInt(10), l_rs.getInt(11),
					l_rs.getString(12), l_rs.getDate(13), l_rs.getBoolean(14),
					l_rs.getBoolean(15), l_rs.getInt(16));
			l_sensors.add(l_sensor);
		}
		l_rs.close();
		l_pstmt.close();

		// get the measure(s) of each sensor
		for (int i = 0; i < l_sensors.size(); i++) {
			ArrayList<Measure> l_measures = new ArrayList<Measure>();
			l_pstmt = m_db
					.prepareStatement("SELECT mea_unit, mea_scale_max, mea_scale_min, mea_eep_shortcut, pk_measure FROM measure WHERE mea_fk_sensor = ?");
			l_pstmt.setInt(1, l_sensors.get(i).getId());
			l_rs = l_pstmt.executeQuery();

			while (l_rs.next()) {
				Measure l_measure = new Measure(l_rs.getInt(5),
						l_rs.getString(1), l_rs.getFloat(2), l_rs.getFloat(3),
						l_rs.getString(4));
				l_measures.add(l_measure);
			}
			l_rs.close();
			l_pstmt.close();
			l_sensors.get(i).setMeasure(l_measures);
		}
		return l_sensors;
	}

	/**
	 * Create a new sensor
	 * 
	 * @param p_sensor
	 *            The sensor object
	 * @param p_groupId
	 *            The group id
	 * @throws SQLException
	 */
	public void addSensor(Sensor p_sensor, int p_groupId) throws SQLException {
		// get the pk_user of the connected user (from username)
		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT pk_user FROM user WHERE usr_username = ?");
		l_pstmt.setString(1, p_sensor.getLastModifier());
		ResultSet l_rs = l_pstmt.executeQuery();

		int l_pk_user = 0;
		if (l_rs.next()) {
			l_pk_user = l_rs.getInt(1);
		}
		l_rs.close();
		l_pstmt.close();

		// get the pk_manufacturer of the manufacturer (from name)
		l_pstmt = m_db
				.prepareStatement("SELECT pk_manufacturer FROM manufacturer WHERE man_name = ?");
		l_pstmt.setString(1, p_sensor.getManufacturer());
		l_rs = l_pstmt.executeQuery();

		int l_pk_manufacturer = 0;
		if (l_rs.next()) {
			l_pk_manufacturer = l_rs.getInt(1);
		}
		l_rs.close();
		l_pstmt.close();

		l_pstmt = m_db
				.prepareStatement(
						"INSERT INTO sensor(sens_name, sens_description, sens_fk_location, sens_fk_manufacturer, sens_eep_rorg, sens_eep_function, sens_eep_type, sens_fk_user_mod, sens_is_actuator, sens_is_in_hybrid_mode, sens_address) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
		l_pstmt.setString(1, p_sensor.getName());
		l_pstmt.setString(2, p_sensor.getDescription());
		l_pstmt.setInt(3, p_sensor.getLocationId());
		l_pstmt.setInt(4, l_pk_manufacturer);
		l_pstmt.setInt(5, p_sensor.getEepRorg());
		l_pstmt.setInt(6, p_sensor.getEepFunction());
		l_pstmt.setInt(7, p_sensor.getEepType());
		l_pstmt.setInt(8, l_pk_user);
		l_pstmt.setBoolean(9, p_sensor.isActuator());
		l_pstmt.setBoolean(10, p_sensor.isHybridMode());
		l_pstmt.setInt(11, p_sensor.getAddress());
		l_pstmt.executeUpdate();

		int l_pk_sensor = 0;
		l_rs = l_pstmt.getGeneratedKeys();
		if (l_rs != null && l_rs.next()) {
			l_pk_sensor = l_rs.getInt(1);
		}
		l_rs.close();
		l_pstmt.close();

		// insert the fk_group and the fk_sensor into the group_sensor table
		l_pstmt = m_db
				.prepareStatement("INSERT INTO group_sensor(fk_group, fk_sensor) VALUES (?, ?)");
		l_pstmt.setInt(1, p_groupId);
		l_pstmt.setInt(2, l_pk_sensor);
		l_pstmt.executeUpdate();
		l_pstmt.close();

		// add the measure(s)
		ArrayList<Measure> l_measures = new ArrayList<Measure>();
		l_measures.addAll(p_sensor.getMeasure());

		for (int i = 0; i < l_measures.size(); i++) {
			l_pstmt = m_db
					.prepareStatement("INSERT INTO measure(mea_unit, mea_scale_max, mea_scale_min, mea_eep_shortcut, mea_fk_sensor) VALUES (?, ?, ?, ?, ?)");

			if (l_measures.get(i).getUnit() == null) {
				l_pstmt.setNull(1, Types.VARCHAR);
				l_pstmt.setNull(2, Types.FLOAT);
				l_pstmt.setNull(3, Types.FLOAT);
			} else {
				l_pstmt.setString(1, l_measures.get(i).getUnit());
				l_pstmt.setFloat(2, l_measures.get(i).getScaleMax());
				l_pstmt.setFloat(3, l_measures.get(i).getScaleMin());
			}
			l_pstmt.setString(4, l_measures.get(i).getEepShortcut());
			l_pstmt.setInt(5, l_pk_sensor);
			l_pstmt.executeUpdate();
			l_pstmt.close();
		}
	}

	/**
	 * Add the sensor to another group
	 * 
	 * @param p_groupId
	 *            The group id
	 * @param p_sensorId
	 *            The sensor id
	 * @throws SQLException 
	 */
	public void addSensorToGroup(int p_groupId, int p_sensorId) throws SQLException {
		PreparedStatement l_pstmt;
		l_pstmt = m_db
				.prepareStatement("INSERT INTO group_sensor(fk_group, fk_sensor) VALUES (?, ?)");
		l_pstmt.setInt(1, p_groupId);
		l_pstmt.setInt(2, p_sensorId);
		l_pstmt.executeUpdate();
		l_pstmt.close();
	}

	/**
	 * Update the specified sensor
	 * 
	 * @param p_sensor
	 *            The sensor object
	 * @param p_id
	 *            The sensor id
	 * @throws SQLException
	 */
	public void updateSensor(Sensor p_sensor, int p_id) throws SQLException {
		// get the pk_user of the connected user (from username)
		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT pk_user FROM user WHERE usr_username = ?");
		l_pstmt.setString(1, p_sensor.getLastModifier());
		ResultSet l_rs = l_pstmt.executeQuery();

		int l_pk_user = 0;
		if (l_rs.next()) {
			l_pk_user = l_rs.getInt(1);
		}
		l_rs.close();
		l_pstmt.close();

		// get the pk_manufacturer of the manufacturer (from name)
		l_pstmt = m_db
				.prepareStatement("SELECT pk_manufacturer FROM manufacturer WHERE man_name = ?");
		l_pstmt.setString(1, p_sensor.getManufacturer());
		l_rs = l_pstmt.executeQuery();

		int l_pk_manufacturer = 0;
		if (l_rs.next()) {
			l_pk_manufacturer = l_rs.getInt(1);
		}
		l_rs.close();
		l_pstmt.close();

		l_pstmt = m_db
				.prepareStatement("UPDATE sensor SET sens_name = ?, sens_description = ?, sens_fk_location = ?, sens_fk_manufacturer = ?,sens_eep_rorg = ?, sens_eep_function = ?, sens_eep_type = ?, sens_fk_user_mod = ?, sens_is_actuator = ?, sens_is_in_hybrid_mode = ?, sens_address = ? WHERE pk_sensor = ?");
		l_pstmt.setString(1, p_sensor.getName());
		l_pstmt.setString(2, p_sensor.getDescription());
		l_pstmt.setInt(3, p_sensor.getLocationId());
		l_pstmt.setInt(4, l_pk_manufacturer);
		l_pstmt.setInt(5, p_sensor.getEepRorg());
		l_pstmt.setInt(6, p_sensor.getEepFunction());
		l_pstmt.setInt(7, p_sensor.getEepType());
		l_pstmt.setInt(8, l_pk_user);
		l_pstmt.setBoolean(9, p_sensor.isActuator());
		l_pstmt.setBoolean(10, p_sensor.isHybridMode());
		l_pstmt.setInt(11, p_sensor.getAddress());
		l_pstmt.setInt(12, p_id);
		l_pstmt.executeUpdate();
		l_pstmt.close();

		ArrayList<String> l_nMeasures = new ArrayList<String>();
		ArrayList<String> l_aMeasures = new ArrayList<String>();

		// get the shortcuts of the stored measures
		l_pstmt = m_db
				.prepareStatement("SELECT mea_eep_shortcut FROM measure WHERE mea_fk_sensor = ?");
		l_pstmt.setInt(1, p_id);
		l_rs = l_pstmt.executeQuery();

		while (l_rs.next()) {
			l_aMeasures.add(l_rs.getString(1));
		}
		l_rs.close();
		l_pstmt.close();

		// get the shortcuts of the new measures
		ArrayList<Measure> l_newMeasures = new ArrayList<Measure>();
		l_newMeasures.addAll(p_sensor.getMeasure());
		for (int i = 0; i < l_newMeasures.size(); i++) {
			l_nMeasures.add(l_newMeasures.get(i).getEepShortcut());
		}

		IEnoceanCommunicator enoceanComm = null;
		try {
			enoceanComm = EnoceanCommunicator.getInstance(
					EnOceanConfig.getSerialPort(), EnOceanConfig.getEepFile());
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (String l_aMeasure : l_aMeasures) {
			if (l_nMeasures.contains(l_aMeasure)) {
				// System.out.println(l_aMeasure + " already exists!");
			} else {
				// get the pk_measure
				l_pstmt = m_db
						.prepareStatement("SELECT pk_measure FROM measure WHERE mea_fk_sensor = ? AND mea_eep_shortcut = ?");
				l_pstmt.setInt(1, p_id);
				l_pstmt.setString(2, l_aMeasure);
				l_rs = l_pstmt.executeQuery();

				int l_pk_measure = 0;
				if (l_rs.next()) {
					l_pk_measure = l_rs.getInt(1);
				}
				l_rs.close();
				l_pstmt.close();

				// verify if a client exists for this measure
				l_pstmt = m_db
						.prepareStatement("SELECT COUNT(pk_client) FROM storage_client WHERE scl_fk_measure = ?");
				l_pstmt.setInt(1, l_pk_measure);
				l_rs = l_pstmt.executeQuery();

				int client = 0;
				if (l_rs.next()) {
					client = l_rs.getInt(1);
				}
				l_rs.close();
				l_pstmt.close();

				if (client != 0) {
					l_pstmt = m_db
							.prepareStatement("DELETE FROM storage_client WHERE scl_fk_measure = ?");
					l_pstmt.setInt(1, l_pk_measure);
					l_pstmt.executeUpdate();
					l_pstmt.close();
				}

				// update the measure to null
				l_pstmt = m_db
						.prepareStatement("UPDATE measure SET mea_fk_data_sensor = NULL WHERE pk_measure = ?");
				l_pstmt.setInt(1, l_pk_measure);
				l_pstmt.executeUpdate();
				l_pstmt.close();

				l_pstmt = m_db
						.prepareStatement("DELETE FROM data_sensor WHERE data_fk_measure = ?");
				l_pstmt.setInt(1, l_pk_measure);
				l_pstmt.executeUpdate();
				l_pstmt.close();

				// delete the measure
				l_pstmt = m_db
						.prepareStatement("DELETE FROM measure WHERE pk_measure = ?");
				l_pstmt.setInt(1, l_pk_measure);
				l_pstmt.executeUpdate();
				l_pstmt.close();
			}
		}

		for (String l_nMeasure : l_nMeasures) {
			if (!l_aMeasures.contains(l_nMeasure)) {
				try {
					// get the infos of the data and create a new measure
					EEPField l_eepField = enoceanComm.getEEPFieldInfo(
							String.format("%02X ", p_sensor.getEepRorg()),
							String.format("%02X ", p_sensor.getEepFunction()),
							String.format("%02X ", p_sensor.getEepType()),
							l_nMeasure, IEnoceanCommunicator.TRANSMISSION);

					Measure l_measure = new Measure(l_eepField
							.getFieldConversions().get(0).getUnit(),
							(float) l_eepField.getFieldConversions().get(0)
									.getScaleMax(),
							(float) l_eepField.getFieldConversions().get(0)
									.getScaleMin(), l_eepField.getShortcut());

					l_pstmt = m_db
							.prepareStatement("INSERT INTO measure(mea_unit, mea_scale_max, mea_scale_min, mea_eep_shortcut, mea_fk_sensor) VALUES (?, ?, ?, ?, ?)");
					l_pstmt.setString(1, l_measure.getUnit());
					l_pstmt.setFloat(2, l_measure.getScaleMax());
					l_pstmt.setFloat(3, l_measure.getScaleMin());
					l_pstmt.setString(4, l_measure.getEepShortcut());
					l_pstmt.setInt(5, p_id);
					l_pstmt.executeUpdate();
					l_pstmt.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Delete the specified sensor
	 * 
	 * @param p_groupId
	 *            The group id
	 * @param p_sensorId
	 *            The sensor id
	 * @throws SQLException
	 */
	public void deleteSensor(int p_groupId, int p_sensorId) throws SQLException {
		// delete the connection between the group and the sensor
		PreparedStatement l_pstmt = m_db
				.prepareStatement("DELETE FROM group_sensor WHERE fk_group = ? AND fk_sensor = ?");
		l_pstmt.setInt(1, p_groupId);
		l_pstmt.setInt(2, p_sensorId);
		l_pstmt.executeUpdate();
		l_pstmt.close();

		// verify if the sensor belong to an another group (if not, it's
		// removed)
		l_pstmt = m_db
				.prepareStatement("SELECT COUNT(fk_group) FROM group_sensor WHERE fk_sensor = ?");
		l_pstmt.setInt(1, p_sensorId);
		ResultSet l_rs = l_pstmt.executeQuery();

		int connection = 1;
		if (l_rs.next()) {
			connection = l_rs.getInt(1);
		}
		l_rs.close();
		l_pstmt.close();

		// if the number of connections is equal to 0, we remove the sensor
		// (it belongs anymore to a group)
		if (connection == 0) {
			ArrayList<Integer> l_pk_measures = new ArrayList<Integer>();

			l_pstmt = m_db
					.prepareStatement("SELECT pk_measure FROM measure WHERE mea_fk_sensor = ?");
			l_pstmt.setInt(1, p_sensorId);
			l_rs = l_pstmt.executeQuery();

			while (l_rs.next()) {
				l_pk_measures.add(l_rs.getInt(1));
			}
			l_rs.close();
			l_pstmt.close();

			// delete the data of the measure(s)
			for (int j = 0; j < l_pk_measures.size(); j++) {
				// verify if a client exists for this measure
				l_pstmt = m_db
						.prepareStatement("SELECT COUNT(pk_client) FROM storage_client WHERE scl_fk_measure = ?");
				l_pstmt.setInt(1, l_pk_measures.get(j));
				l_rs = l_pstmt.executeQuery();

				int client = 0;
				if (l_rs.next()) {
					client = l_rs.getInt(1);
				}
				l_rs.close();
				l_pstmt.close();

				if (client != 0) {
					l_pstmt = m_db
							.prepareStatement("DELETE FROM storage_client WHERE scl_fk_measure = ?");
					l_pstmt.setInt(1, l_pk_measures.get(j));
					l_pstmt.executeUpdate();
					l_pstmt.close();
				}

				// update the measure to null
				l_pstmt = m_db
						.prepareStatement("UPDATE measure SET mea_fk_data_sensor = NULL WHERE pk_measure = ?");
				l_pstmt.setInt(1, l_pk_measures.get(j));
				l_pstmt.executeUpdate();
				l_pstmt.close();

				l_pstmt = m_db
						.prepareStatement("DELETE FROM data_sensor WHERE data_fk_measure = ?");
				l_pstmt.setInt(1, l_pk_measures.get(j));
				l_pstmt.executeUpdate();
				l_pstmt.close();

				// delete the measure
				l_pstmt = m_db
						.prepareStatement("DELETE FROM measure WHERE pk_measure = ?");
				l_pstmt.setInt(1, l_pk_measures.get(j));
				l_pstmt.executeUpdate();
				l_pstmt.close();
			}

			// delete the location (if it doesn't belong to another sensor / has
			// child(s))
			l_pstmt = m_db
					.prepareStatement("SELECT sens_fk_location FROM sensor WHERE pk_sensor = ?");
			l_pstmt.setInt(1, p_sensorId);
			l_rs = l_pstmt.executeQuery();

			int l_locationId = 5;
			if (l_rs.next()) {
				l_locationId = l_rs.getInt(1);
			}
			l_rs.close();
			l_pstmt.close();

			// delete the sensor
			l_pstmt = m_db
					.prepareStatement("DELETE FROM sensor WHERE pk_sensor = ?");
			l_pstmt.setInt(1, p_sensorId);
			l_pstmt.executeUpdate();
			l_pstmt.close();

			deleteLocation(l_locationId);
		}
	}

	/**
	 * Get all the registered manufacturers
	 * 
	 * @return ArrayList of manufacture objects
	 * @throws SQLException
	 */
	public ArrayList<Manufacturer> getManufacturers() throws SQLException {
		ArrayList<Manufacturer> l_manufacturers = new ArrayList<Manufacturer>();

		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT pk_manufacturer, man_name FROM manufacturer");
		ResultSet l_rs = l_pstmt.executeQuery();

		while (l_rs.next()) {
			Manufacturer l_manufacturer = new Manufacturer(l_rs.getInt(1),
					l_rs.getString(2));
			l_manufacturers.add(l_manufacturer);
		}

		l_rs.close();
		l_pstmt.close();
		return l_manufacturers;
	}

	/**
	 * Get all the registered locations from root
	 * 
	 * @return ArrayList of location objects
	 * @throws SQLException
	 */
	public ArrayList<Location> getLocationRoot() throws SQLException {
		ArrayList<Location> l_locations = new ArrayList<Location>();

		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT pk_location, loc_name, loc_type_name, loc_type_img_url, loc_path FROM location INNER JOIN location_type ON loc_fk_type = pk_location_type WHERE loc_parent_location IS NULL");
		ResultSet l_rs = l_pstmt.executeQuery();

		while (l_rs.next()) {
			Location l_location = new Location(l_rs.getInt(1),
					l_rs.getString(2), l_rs.getString(3), l_rs.getString(4),
					l_rs.getString(5));
			l_locations.add(l_location);
		}
		l_rs.close();
		l_pstmt.close();
		
		return l_locations;
	}

	/**
	 * Get all the child locations
	 * 
	 * @param p_id
	 *            The parent location id
	 * @return ArrayList of location objects
	 * @throws SQLException
	 */
	public ArrayList<Location> getLocationChilds(int p_id) throws SQLException {
		ArrayList<Location> l_locations = new ArrayList<Location>();

		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT pk_location, loc_name, loc_type_name, loc_type_img_url, loc_path FROM location INNER JOIN location_type ON loc_fk_type = pk_location_type WHERE loc_parent_location = ?");
		l_pstmt.setInt(1, p_id);
		ResultSet l_rs = l_pstmt.executeQuery();

		while (l_rs.next()) {
			Location l_location = new Location(l_rs.getInt(1),
					l_rs.getString(2), l_rs.getString(3), l_rs.getString(4),
					l_rs.getString(5));
			l_locations.add(l_location);
		}
		l_rs.close();
		l_pstmt.close();
		
		return l_locations;
	}

	/**
	 * Get the specified location
	 * 
	 * @param p_id
	 *            The location id
	 * @return Location object
	 * @throws SQLException
	 */
	public Location getLocationFromId(int p_id) throws SQLException {
		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT pk_location, loc_name, loc_type_name, loc_type_img_url, loc_path FROM location INNER JOIN location_type ON loc_fk_type = pk_location_type WHERE pk_location = ?");
		l_pstmt.setInt(1, p_id);
		ResultSet l_rs = l_pstmt.executeQuery();

		Location l_location = null;

		if (l_rs.next()) {
			l_location = new Location(l_rs.getInt(1), l_rs.getString(2),
					l_rs.getString(3), l_rs.getString(4), l_rs.getString(5));
		}

		l_rs.close();
		l_pstmt.close();
		
		return l_location;
	}

	/**
	 * Get all the registered location types
	 * 
	 * @return ArrayList of location types objects
	 * @throws SQLException
	 */
	public ArrayList<String> getLocationTypes() throws SQLException {
		ArrayList<String> l_locationTypes = new ArrayList<String>();

		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT loc_type_name FROM location_type");
		ResultSet l_rs = l_pstmt.executeQuery();

		while (l_rs.next()) {
			l_locationTypes.add(l_rs.getString(1));
		}
		l_rs.close();
		l_pstmt.close();
		
		return l_locationTypes;
	}

	/**
	 * Create a new child location
	 * 
	 * @param p_location
	 *            The location object
	 * @param p_parentId
	 *            The parent location id
	 * @throws SQLException
	 */
	public String addLocation(Location p_location, int p_parentId)
			throws SQLException {
		// get the location type id (from name)
		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT pk_location_type FROM location_type WHERE loc_type_name = ?");
		l_pstmt.setString(1, p_location.getTypeName());
		ResultSet l_rs = l_pstmt.executeQuery();

		int l_typeId = 0;
		if (l_rs.next()) {
			l_typeId = l_rs.getInt(1);
		}
		l_rs.close();
		l_pstmt.close();

		l_pstmt = m_db
				.prepareStatement(
						"INSERT INTO location(loc_parent_location, loc_name, loc_fk_type) VALUES (?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
		l_pstmt.setInt(1, p_parentId);
		l_pstmt.setString(2, p_location.getName());
		l_pstmt.setInt(3, l_typeId);
		l_pstmt.executeUpdate();

		int l_locationId = 0;
		l_rs = l_pstmt.getGeneratedKeys();
		if (l_rs != null && l_rs.next()) {
			l_locationId = l_rs.getInt(1);
		}
		l_rs.close();
		l_pstmt.close();

		int l_locationParentId = 0;
		l_locationParentId = l_locationId;

		String l_path = getLocationPath(l_locationParentId);
		// update the location with the path found below
		l_pstmt = m_db
				.prepareStatement("UPDATE location SET loc_path =  ? WHERE pk_location = ?");
		l_pstmt.setString(1, l_path);
		l_pstmt.setInt(2, l_locationParentId);
		l_pstmt.executeUpdate();
		l_pstmt.close();

		return p_location.getName() + "." + l_path;
	}

	/**
	 * Add a root location
	 * 
	 * @param p_location
	 *            The location object
	 * @throws SQLException
	 */
	public void addLocation(Location p_location) throws SQLException {
		// get the location type id (from name)
		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT pk_location_type FROM location_type WHERE loc_type_name = ?");
		l_pstmt.setString(1, p_location.getTypeName());
		ResultSet l_rs = l_pstmt.executeQuery();

		int l_typeId = 0;
		if (l_rs.next()) {
			l_typeId = l_rs.getInt(1);
		}
		l_rs.close();
		l_pstmt.close();

		l_pstmt = m_db
				.prepareStatement("INSERT INTO location (loc_name, loc_fk_type) VALUES (?, ?)");
		l_pstmt.setString(1, p_location.getName());
		l_pstmt.setInt(2, l_typeId);
		l_pstmt.executeUpdate();
		l_pstmt.close();
	}

	/**
	 * Update the specified location
	 * 
	 * @param p_location
	 *            The location object
	 * @param p_id
	 *            The location id
	 * @throws SQLException
	 */
	public void updateLocation(Location p_location, int p_id)
			throws SQLException {
		// get the id of the location type name
		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT pk_location_type FROM location_type WHERE loc_type_name = ?");
		l_pstmt.setString(1, p_location.getTypeName());
		ResultSet l_rs = l_pstmt.executeQuery();

		int l_typeId = 0;
		if (l_rs.next()) {
			l_typeId = l_rs.getInt(1);
		}
		l_rs.close();
		l_pstmt.close();

		// update the location
		l_pstmt = m_db
				.prepareStatement("UPDATE location SET loc_name =  ?, loc_fk_type = ? WHERE pk_location = ?");
		l_pstmt.setString(1, p_location.getName());
		l_pstmt.setInt(2, l_typeId);
		l_pstmt.setInt(3, p_id);
		l_pstmt.executeUpdate();
		l_pstmt.close();

		updateChildsPath(p_id);
	}

	/**
	 * Update the path of the registered child locations
	 * 
	 * @param p_id
	 *            The parent location id
	 * @throws SQLException
	 */
	private void updateChildsPath(int p_id) throws SQLException {
		ArrayList<Integer> l_pk_locations = new ArrayList<Integer>();
		PreparedStatement l_pstmt = m_db
				.prepareStatement("select pk_location from location where loc_parent_location = ?");
		l_pstmt.setInt(1, p_id);
		ResultSet l_rs = l_pstmt.executeQuery();

		while (l_rs.next())
			l_pk_locations.add(l_rs.getInt(1));
		l_rs.close();
		l_pstmt.close();

		for (int i = 0; i < l_pk_locations.size(); i++) {
			String l_path = getLocationPath(l_pk_locations.get(i));
			l_pstmt = m_db
					.prepareStatement("update location set loc_path = ? where pk_location = ?");
			l_pstmt.setString(1, l_path);
			l_pstmt.setInt(2, l_pk_locations.get(i));
			l_pstmt.executeUpdate();
			l_pstmt.close();
			updateChildsPath(l_pk_locations.get(i));
		}
	}

	/**
	 * Delete the specified location
	 * 
	 * @param p_id
	 *            The location id
	 * @throws SQLException
	 */
	public void deleteLocation(int p_id) throws SQLException {
		// if the location is "alone" (no child, no sensor), we can remove it.
		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT count(pk_sensor) FROM sensor WHERE sens_fk_location = ?");
		l_pstmt.setInt(1, p_id);
		ResultSet l_rs = l_pstmt.executeQuery();

		int l_pk_sensor = 0;
		if (l_rs.next()) {
			l_pk_sensor = l_rs.getInt(1);
		}
		l_rs.close();
		l_pstmt.close();

		// check if the location has child(s)
		l_pstmt = m_db
				.prepareStatement("SELECT count(pk_location) FROM location WHERE loc_parent_location = ?");
		l_pstmt.setInt(1, p_id);
		l_rs = l_pstmt.executeQuery();

		int l_locationChilds = 0;
		if (l_rs.next()) {
			l_locationChilds = l_rs.getInt(1);
		}
		l_rs.close();
		l_pstmt.close();

		if (l_pk_sensor == 0 && l_locationChilds == 0) {
			l_pstmt = m_db
					.prepareStatement("DELETE FROM location WHERE pk_location = ?");
			l_pstmt.setInt(1, p_id);
			l_pstmt.executeUpdate();
			l_pstmt.close();
		}
	}

	/**
	 * Return the path of the specified location
	 * 
	 * @param p_id
	 *            The location id
	 * @return
	 * @throws SQLException
	 */
	public String getLocationPath(int p_id) throws SQLException {
		StringBuffer l_stringBuffer = new StringBuffer();
		Boolean l_isNull = new Boolean(false);

		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT loc_parent_location FROM location WHERE pk_location = ?");
		l_pstmt.setInt(1, p_id);
		ResultSet l_rs = l_pstmt.executeQuery();

		if (l_rs.next()) {
			p_id = l_rs.getInt(1);

			// get the child
			PreparedStatement l_pstmtParent = m_db
					.prepareStatement("SELECT loc_parent_location, loc_name FROM location WHERE pk_location = ?");
			l_pstmtParent.setInt(1, p_id);
			ResultSet l_rsParent = l_pstmtParent.executeQuery();

			if (l_rsParent.next()) {
				p_id = l_rsParent.getInt(1);
				l_stringBuffer.append(l_rsParent.getString(2));
			} else {
				l_isNull = true;
			}
			l_rsParent.close();
			l_pstmtParent.close();
		} else {
			l_isNull = true;
		}
		l_rs.close();
		l_pstmt.close();

		while (l_isNull == false) {
			l_pstmt = m_db
					.prepareStatement("SELECT loc_parent_location, loc_name FROM location WHERE pk_location = ?");
			l_pstmt.setInt(1, p_id);
			l_rs = l_pstmt.executeQuery();

			if (l_rs.next()) {
				p_id = l_rs.getInt(1);
				l_stringBuffer.append(".");
				l_stringBuffer.append(l_rs.getString(2));
			} else {
				l_isNull = true;
			}
			l_rs.close();
			l_pstmt.close();
		}

		return l_stringBuffer.toString();
	}

	/**
	 * The user username
	 * 
	 * @return User object
	 * @throws SQLException
	 */
	public User getUserFromUsername(String p_username) throws SQLException {
		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT pk_user, usr_username, usr_password, usr_first_name, usr_last_name, usr_email, usr_is_admin, usr_is_active FROM user WHERE usr_username = ?");
		l_pstmt.setString(1, p_username);
		ResultSet l_rs = l_pstmt.executeQuery();

		User l_user = null;
		if (l_rs.next()) {
			l_user = new User(l_rs.getInt(1), l_rs.getString(2),
					l_rs.getString(3), l_rs.getString(4), l_rs.getString(5),
					l_rs.getString(6), l_rs.getBoolean(7), l_rs.getBoolean(8));
		}
		l_rs.close();
		l_pstmt.close();
		
		return l_user;
	}

	/**
	 * Find a measure
	 * 
	 * @param p_SensorName
	 *            The name of the sensor
	 * @param p_SensorLocation
	 *            The location path of the sensor
	 * @param p_Shortcut
	 *            The shortcut name of the measure
	 * @return THe id of the measure
	 * @throws Exception
	 */
	public int findMeasure(String p_SensorName, String p_SensorLocation,
			String p_Shortcut) throws Exception {
		PreparedStatement l_pstmt = m_db
				.prepareStatement("select pk_measure from measure inner join sensor on mea_fk_sensor = pk_sensor inner join location on sens_fk_location = pk_location where LOWER(mea_eep_shortcut) = ? and LOWER(sens_name) = ? and LOWER(CONCAT(loc_name, '.', loc_path)) = ?");
		l_pstmt.setString(1, p_Shortcut.toLowerCase());
		l_pstmt.setString(2, p_SensorName.toLowerCase());
		l_pstmt.setString(3, p_SensorLocation.toLowerCase());
		ResultSet l_rs = l_pstmt.executeQuery();

		int l_pkMeasure = -1;

		if (l_rs.next())
			l_pkMeasure = l_rs.getInt(1);

		l_rs.close();
		l_pstmt.close();

		if (l_pkMeasure == -1)
			throw new Exception("Measure does not exist");
		return l_pkMeasure;
	}

	/**
	 * Register a client of stored data
	 * 
	 * @param p_IdMeasure
	 *            The id of the measure
	 * @param p_Days
	 *            The max days to conserve data
	 * @param p_Referer
	 *            The id of the client
	 * @throws SQLException
	 */
	public void addClient(int p_IdMeasure, int p_Days, String p_Referer)
			throws SQLException {
		// check if client already registered for the measure
		PreparedStatement l_pstmt = m_db
				.prepareStatement("select pk_client from storage_client where scl_referer = ? and scl_fk_measure = ?");
		l_pstmt.setString(1, p_Referer);
		l_pstmt.setInt(2, p_IdMeasure);
		ResultSet l_rs = l_pstmt.executeQuery();

		int l_pkClient = -1;

		if (l_rs.next())
			l_pkClient = l_rs.getInt(1);

		l_rs.close();
		l_pstmt.close();

		// add measure client
		if (l_pkClient != -1) {
			l_pstmt = m_db
					.prepareStatement("update storage_client set scl_days = ? where pk_client = ?");
			l_pstmt.setInt(1, p_Days);
			l_pstmt.setInt(2, l_pkClient);
			l_pstmt.executeUpdate();
			l_pstmt.close();
		} else {
			// add client
			l_pstmt = m_db
					.prepareStatement("insert into storage_client (scl_referer, scl_days, scl_fk_measure) values (?, ?, ?)");
			l_pstmt.setString(1, p_Referer);
			l_pstmt.setInt(2, p_Days);
			l_pstmt.setInt(3, p_IdMeasure);
			l_pkClient = l_pstmt.executeUpdate();
			l_pstmt.close();
		}

	}

	/**
	 * Remove a client of stored data
	 * 
	 * @param p_IdMeasure
	 *            The id of the measure
	 * @param p_Referer
	 *            The id of the client
	 * @throws SQLException
	 */
	public void removeClient(int p_IdMeasure, String p_Referer)
			throws SQLException {
		PreparedStatement l_pstmt;

		// delete client
		l_pstmt = m_db
				.prepareStatement("delete from storage_client where scl_referer = ? and scl_fk_measure = ?");
		l_pstmt.setString(1, p_Referer);
		l_pstmt.setInt(2, p_IdMeasure);
		l_pstmt.executeUpdate();
		l_pstmt.close();

		deleteDataMeasure(p_IdMeasure);
	}

	private void deleteData(int p_IdMeasure) throws SQLException {
		PreparedStatement l_pstmt = m_db
				.prepareStatement("delete from data_sensor where data_fk_measure = ? and pk_data_sensor != (select mea_fk_data_sensor from measure where pk_measure = ?)");
		l_pstmt.setInt(1, p_IdMeasure);
		l_pstmt.setInt(2, p_IdMeasure);
		l_pstmt.executeUpdate();
		l_pstmt.close();
	}

	private void shrinkData(int p_IdMeasure) throws SQLException {
		// select max days
		PreparedStatement l_pstmt = m_db
				.prepareStatement("select max(scl_days) from storage_client where scl_fk_measure = ?");
		l_pstmt.setInt(1, p_IdMeasure);
		ResultSet l_rs = l_pstmt.executeQuery();

		int l_MaxDays = -1;
		if (l_rs.next())
			l_MaxDays = l_rs.getInt(1);

		l_rs.close();
		l_pstmt.close();

		if (l_MaxDays != -1) {
			// shrink data according to max days
			l_pstmt = m_db
					.prepareStatement("delete from data_sensor where data_fk_measure = ? and data_timestamp < ?");
			l_pstmt.setInt(1, p_IdMeasure);
			Calendar l_cal = Calendar.getInstance();
			l_cal.setTime(new Date());
			l_cal.add(Calendar.DATE, l_MaxDays * -1);
			l_pstmt.setDate(2, new java.sql.Date(l_cal.getTime().getTime()));
			l_pstmt.executeUpdate();
			l_pstmt.close();
		} else {
			// shrink data according to average days
			l_pstmt = m_db.prepareStatement("select avg(scl_days) from storage_client");
			l_rs = l_pstmt.executeQuery();

			int l_avgDays = -1;

			if (l_rs.next())
				l_avgDays = l_rs.getInt(1);

			l_rs.close();
			l_pstmt.close();

			l_pstmt = m_db
					.prepareStatement("delete from data_sensor where data_fk_measure = ? and data_timestamp < ?");
			l_pstmt.setInt(1, p_IdMeasure);
			Calendar l_cal = Calendar.getInstance();
			l_cal.setTime(new Date());
			l_cal.add(Calendar.DATE, l_avgDays * -1);
			l_pstmt.setDate(2, new java.sql.Date(l_cal.getTime().getTime()));
			l_pstmt.executeUpdate();
			l_pstmt.close();
		}

	}

	public void deleteDataMeasure(int p_idMeasure) throws SQLException {
		// check if other clients
		PreparedStatement l_pstmt = m_db
				.prepareStatement("select pk_client from storage_client where scl_fk_measure = ?");
		l_pstmt.setInt(1, p_idMeasure);
		ResultSet l_rs = l_pstmt.executeQuery();

		if (!l_rs.next()) {
			l_rs.close();
			l_pstmt.close();
			// delete data and storage
			deleteData(p_idMeasure);
		} else {
			l_rs.close();
			l_pstmt.close();
			shrinkData(p_idMeasure);
		}
	}

	/**
	 * Get the data for a time range
	 * 
	 * @param p_IdMeasure
	 *            The id of the measure
	 * @param p_Start
	 *            The start date (most recent)
	 * @param p_End
	 *            The end date
	 * @return ArrayList of storage data
	 * @throws Exception
	 */
	public ArrayList<StorageData> getStorage(int p_IdMeasure, Date p_Start,
			Date p_End) throws Exception {
		ArrayList<StorageData> l_results = new ArrayList<StorageData>();
		PreparedStatement l_pstmt;
		l_pstmt = m_db
				.prepareStatement("select data_value, data_timestamp from data_sensor where data_fk_measure = ? and data_timestamp between ? and ?");
		l_pstmt.setInt(1, p_IdMeasure);
		l_pstmt.setTimestamp(2, new java.sql.Timestamp(p_Start.getTime()));
		l_pstmt.setTimestamp(3, new java.sql.Timestamp(p_End.getTime()));

		ResultSet l_rs = l_pstmt.executeQuery();

		while (l_rs.next())
			l_results.add(new StorageData(l_rs.getString(1), l_rs
					.getTimestamp(2)));

		l_rs.close();
		l_pstmt.close();

		updateLastRead(p_IdMeasure);

		return l_results;
	}

	/**
	 * Get the last X days of data
	 * 
	 * @param p_IdMeasure
	 *            The id of the measure
	 * @param p_Days
	 *            The number of days
	 * @return ArrayList of storage data
	 * @throws Exception
	 */
	public ArrayList<StorageData> getLastStorage(int p_IdMeasure, int p_Days)
			throws Exception {
		Calendar l_cal = Calendar.getInstance();
		Date l_now = new Date();
		l_cal.setTime(l_now);
		l_cal.add(Calendar.DATE, p_Days * -1);
		return getStorage(p_IdMeasure, l_cal.getTime(), l_now);
	}

	/**
	 * Get the last value received for the measure
	 * 
	 * @param p_IdMeasure
	 *            The measure to look for
	 * @return The last data of the measure
	 * @throws SQLException
	 */
	public StorageData getLastStorage(int p_IdMeasure) throws SQLException {
		StorageData l_data = null;
		PreparedStatement l_pstmt = m_db
				.prepareStatement("select data_value, data_timestamp from data_sensor inner join measure on mea_fk_data_sensor = pk_data_sensor where pk_measure = ?");
		l_pstmt.setInt(1, p_IdMeasure);

		ResultSet l_rs = l_pstmt.executeQuery();

		if (l_rs.next())
			l_data = new StorageData(new Double(l_rs.getDouble(1)).toString(),
					l_rs.getTimestamp(2));
		l_rs.close();
		l_pstmt.close();

		return l_data;
	}

	/**
	 * List the available measures of a sensor
	 * 
	 * @param p_SensorName
	 *            The name of the sensor
	 * @param p_SensorLocation
	 *            The location of the sensor
	 * @return A list of measures
	 * @throws SQLException
	 */
	public ArrayList<MeasureDescription> listMeasures(String p_SensorName,
			String p_SensorLocation) throws SQLException {
		ArrayList<MeasureDescription> l_Measures = new ArrayList<MeasureDescription>();
		PreparedStatement l_pstmt = m_db
				.prepareStatement("select pk_measure, mea_unit, mea_scale_max, mea_scale_min, mea_eep_shortcut from measure inner join sensor on mea_fk_sensor = pk_sensor inner join location on sens_fk_location = pk_location where LOWER(CONCAT(loc_name, '.', loc_path)) = ? and LOWER(sens_name) = ?");
		l_pstmt.setString(1, p_SensorLocation.toLowerCase());
		l_pstmt.setString(2, p_SensorName.toLowerCase());

		ResultSet l_rs = l_pstmt.executeQuery();

		while (l_rs.next()) {
			l_Measures.add(new MeasureDescription(l_rs.getInt(1), l_rs
					.getString(2), l_rs.getDouble(3), l_rs.getDouble(4), l_rs
					.getString(5)));
		}
		l_rs.close();
		l_pstmt.close();

		return l_Measures;
	}

	/**
	 * List the available measures of a sensor
	 * 
	 * @param p_IdSensor
	 *            The Id of the sensor
	 * @return A list of measures
	 * @throws SQLException
	 */
	public ArrayList<MeasureDescription> listMeasures(int p_IdSensor)
			throws SQLException {
		ArrayList<MeasureDescription> l_Measures = new ArrayList<MeasureDescription>();
		PreparedStatement l_pstmt = m_db
				.prepareStatement("select pk_measure, mea_unit, mea_scale_max, mea_scale_min, mea_eep_shortcut from measure inner join sensor on mea_fk_sensor = pk_sensor where pk_sensor = ?");
		l_pstmt.setInt(1, p_IdSensor);

		ResultSet l_rs = l_pstmt.executeQuery();

		while (l_rs.next()) {
			l_Measures.add(new MeasureDescription(l_rs.getInt(1), l_rs
					.getString(2), l_rs.getDouble(3), l_rs.getDouble(4), l_rs
					.getString(5)));
		}
		l_rs.close();
		l_pstmt.close();

		return l_Measures;
	}

	/**
	 * List the children of a location (sensors and sub-locations)
	 * 
	 * @param p_Location
	 *            The starting location
	 * @return The list of children of the location
	 * @throws SQLException
	 */
	public ArrayList<ChildDescription> listChildren(String p_Location)
			throws SQLException {
		ArrayList<ChildDescription> l_Children = new ArrayList<ChildDescription>();

		PreparedStatement l_pstmt = m_db
				.prepareStatement("select loc_name from location where LOWER(loc_path) = ?");
		l_pstmt.setString(1, p_Location.toLowerCase());

		ResultSet l_rs = l_pstmt.executeQuery();
		while (l_rs.next())
			l_Children.add(new ChildDescription(l_rs.getString(1), false));
		l_rs.close();
		l_pstmt.close();

		l_pstmt = m_db
				.prepareStatement("select sens_name from sensor inner join location on sens_fk_location = pk_location and LOWER(CONCAT(loc_name, '.', loc_path)) = ?");
		l_pstmt.setString(1, p_Location.toLowerCase());
		l_rs = l_pstmt.executeQuery();
		while (l_rs.next())
			l_Children.add(new ChildDescription(l_rs.getString(1), true));
		l_rs.close();
		l_pstmt.close();

		return l_Children;
	}

	/**
	 * List the measures that have storage activated
	 * 
	 * @return The group numbers
	 * @throws SQLException
	 */
	public ArrayList<Integer> getStorageGroups() throws SQLException {
		ArrayList<Integer> l_groups = new ArrayList<Integer>();
		PreparedStatement l_pstmt = m_db
				.prepareStatement("select distinct pk_measure from measure inner join storage_client on scl_fk_measure = pk_measure");
		ResultSet l_rs = l_pstmt.executeQuery();

		while (l_rs.next()) {
			l_groups.add(l_rs.getInt(1));
		}
		l_rs.close();
		l_pstmt.close();

		return l_groups;
	}

	private void updateLastRead(int p_IdMeasure) throws SQLException {
		PreparedStatement l_pstmt = m_db
				.prepareStatement("update measure set mea_last_read=now() where pk_measure = ?");
		l_pstmt.setInt(1, p_IdMeasure);
		l_pstmt.executeUpdate();
		l_pstmt.close();
	}

	/**
	 * Get the EEP information of a sensor
	 * 
	 * @param p_Address
	 *            The physical address
	 * @return The sensor EEP
	 * @throws SQLException
	 */
	public SensorEEP getEEP(long p_Address) throws SQLException {
		SensorEEP sensorEEP = null;
		PreparedStatement l_pstmt = m_db
				.prepareStatement("select pk_sensor, sens_eep_rorg, sens_eep_function, sens_eep_type from sensor where sens_address = ?");
		l_pstmt.setLong(1, p_Address);

		ResultSet l_rs = l_pstmt.executeQuery();
		if (l_rs.next())
			sensorEEP = new SensorEEP(l_rs.getInt(1), l_rs.getInt(2),
					l_rs.getInt(3), l_rs.getInt(4));
		l_rs.close();
		l_pstmt.close();

		return sensorEEP;
	}

	/**
	 * Add data
	 * 
	 * @param idMeasure
	 *            The id of the measure concerned
	 * @param value
	 *            The value of the measure
	 * @param date
	 *            The timestamp of the measure
	 * @return The id of inserted row
	 * @throws SQLException
	 */
	public int addStorage(int idMeasure, double value, Date date)
			throws SQLException {
		int l_idData = -1;
		PreparedStatement l_pstmt = m_db
				.prepareStatement(
						"insert into data_sensor (data_value, data_timestamp, data_fk_measure) values (?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
		l_pstmt.setDouble(1, value);
		l_pstmt.setTimestamp(2, new Timestamp(date.getTime()));
		l_pstmt.setInt(3, idMeasure);

		l_pstmt.executeUpdate();
		ResultSet l_rs = l_pstmt.getGeneratedKeys();
		if (l_rs != null && l_rs.next()) {
			l_idData = l_rs.getInt(1);
		}
		l_rs.close();
		l_pstmt.close();

		return l_idData;
	}

	/**
	 * Set the last received data of a measure
	 * 
	 * @param idMeasure
	 *            The measure id
	 * @param idData
	 *            The data id
	 * @throws SQLException
	 */
	public void setLastMeasureData(int idMeasure, int idData)
			throws SQLException {
		PreparedStatement l_pstmt = m_db
				.prepareStatement("update measure set mea_fk_data_sensor = ? where pk_measure = ?");
		l_pstmt.setInt(1, idData);
		l_pstmt.setInt(2, idMeasure);
		l_pstmt.executeUpdate();
		l_pstmt.close();
	}

	/**
	 * Check if sensor is in hybrid mode
	 * 
	 * @param sensorAddress
	 *            The physical adress
	 * @return Sensor in hybrid mode
	 * @throws SQLException
	 */
	public boolean isHybridMode(long sensorAddress) throws SQLException {
		boolean isHybrid = false;
		PreparedStatement l_pstmt = m_db
				.prepareStatement("select sens_is_in_hybrid_mode from sensor where sens_address = ?");
		l_pstmt.setLong(1, sensorAddress);
		ResultSet l_rs = l_pstmt.executeQuery();

		if (l_rs.next())
			isHybrid = l_rs.getBoolean(1);
		l_rs.close();
		l_pstmt.close();

		return isHybrid;
	}

	/**
	 * Get the physical address of a device by its location
	 * 
	 * @param p_SensorName
	 *            The name of the sensor
	 * @param p_SensorLocation
	 *            The location of the sensor
	 * @return The physical address
	 * @throws SQLException
	 */
	public long getAddress(String p_SensorName, String p_SensorLocation)
			throws SQLException {
		long l_addr = -1;
		PreparedStatement l_pstmt = m_db
				.prepareStatement("select sens_address from sensor inner join location on sens_fk_location = pk_location where LOWER(CONCAT(loc_name, '.', loc_path)) = ? and LOWER(sens_name) = ?");
		l_pstmt.setString(1, p_SensorLocation.toLowerCase());
		l_pstmt.setString(2, p_SensorName.toLowerCase());

		ResultSet l_rs = l_pstmt.executeQuery();
		if (l_rs.next())
			l_addr = l_rs.getLong(1);
		l_rs.close();
		l_pstmt.close();

		return l_addr;
	}

	/**
	 * Get the manufacturer code for a device
	 * 
	 * @param p_Address
	 *            The address of the device
	 * @return The manufacturer code
	 * @throws SQLException
	 */
	public int getManufacturer(long p_Address) throws SQLException {
		int l_manufac = -1;
		PreparedStatement l_pstmt = m_db
				.prepareStatement("select sens_fk_manufacturer from sensor where sens_address = ?");
		l_pstmt.setLong(1, p_Address);

		ResultSet l_rs = l_pstmt.executeQuery();
		if (l_rs.next())
			l_manufac = l_rs.getInt(1);
		l_rs.close();
		l_pstmt.close();

		return l_manufac;
	}

	/**
	 * Get all the registered datas
	 * 
	 * @param p_measureId
	 *            The id of the measure
	 * @param p_dateTo
	 *            Start date for data
	 * @param p_dateFrom
	 *            End date for data
	 * @return JSON array : pk_data_sensor, data_value, data_timestamp
	 * @throws SQLException
	 */
	public ArrayList<Data> getDatas(int p_measureId, Date p_dateFrom,
			Date p_dateTo) throws SQLException {
		ArrayList<Data> l_datas = new ArrayList<Data>();

		PreparedStatement l_pstmt = m_db
				.prepareStatement("SELECT pk_data_sensor, data_value, data_timestamp, data_fk_measure FROM data_sensor WHERE data_fk_measure = ? and data_timestamp between ? and ?");
		l_pstmt.setInt(1, p_measureId);
		l_pstmt.setTimestamp(2, new Timestamp(p_dateFrom.getTime()));
		l_pstmt.setTimestamp(3, new Timestamp(p_dateTo.getTime()));

		ResultSet l_rs = l_pstmt.executeQuery();

		while (l_rs.next()) {
			Data l_data = new Data(l_rs.getInt(1), l_rs.getFloat(2),
					l_rs.getTimestamp(3), l_rs.getInt(4));
			l_datas.add(l_data);
		}
		l_rs.close();
		l_pstmt.close();

		return l_datas;
	}
}
