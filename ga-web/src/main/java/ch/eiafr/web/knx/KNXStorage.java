package ch.eiafr.web.knx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eiafr.web.knx.admin.Subscriber;

import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.dptxlator.TranslatorTypes;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.process.ProcessEvent;

public class KNXStorage implements Observer {
	/**
	 * Manage the storage of data in the database
	 * 
	 * @author gb
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(KNXStorage.class);
	private static KNXStorage m_KnxStorage = new KNXStorage();
	private Connection m_db = null;

	private KNXStorage() {
	}

	public static KNXStorage getInstance() {
		return m_KnxStorage;
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
	 * Register a client of stored data
	 * 
	 * @param p_Datapoint
	 *            The datapoint concerned
	 * @param p_Days
	 *            The max days to conserve data
	 * @param p_Referer
	 *            The id of the client
	 * @throws SQLException
	 */
	public void addStorage(Datapoint p_Datapoint, int p_Days, String p_Referer, String p_DPName)
			throws SQLException {
		// Check if datapoint already registered for storage
		PreparedStatement l_pstmt = m_db
				.prepareStatement("select DPS_PK_DPStorage from DPStorage where DPS_GroupAddress = ? and DPS_DPT = ?");
		l_pstmt.setInt(1, p_Datapoint.getMainAddress().getRawAddress());
		l_pstmt.setString(2, p_Datapoint.getDPT());
		ResultSet l_rs = l_pstmt.executeQuery();

		int l_pkStorage = -1;

		if (l_rs.next())
			l_pkStorage = l_rs.getInt(1);

		l_rs.close();

		// add datapoint storage
		if (l_pkStorage == -1) {
			l_pstmt = m_db
					.prepareStatement(
							"insert into DPStorage (DPS_GroupAddress, DPS_DPT, DPS_DPName) values (?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS);
			l_pstmt.setInt(1, p_Datapoint.getMainAddress().getRawAddress());
			l_pstmt.setString(2, p_Datapoint.getDPT());
			l_pstmt.setString(3, p_DPName);
			l_pstmt.executeUpdate();
			l_rs = l_pstmt.getGeneratedKeys();
			if (l_rs != null && l_rs.next()) {
				l_pkStorage = l_rs.getInt(1);
			}
			l_rs.close();
		}

		// check if client already registered for the datapoint
		l_pstmt = m_db
				.prepareStatement("select CL_PK_Client from DPClient where CL_Referer = ? and CL_FK_DPStorage = ?");
		l_pstmt.setString(1, p_Referer);
		l_pstmt.setInt(2, l_pkStorage);
		l_rs = l_pstmt.executeQuery();

		int l_pkClient = -1;

		if (l_rs.next())
			l_pkClient = l_rs.getInt(1);

		l_rs.close();

		// add datapoint client
		if (l_pkClient != -1) {
			l_pstmt = m_db
					.prepareStatement("update DPClient set CL_Days = ? where CL_PK_DPClient = ?");
			l_pstmt.setInt(1, p_Days);
			l_pstmt.setInt(2, l_pkClient);
			l_pstmt.executeUpdate();
		} else {
			// add client
			l_pstmt = m_db
					.prepareStatement("insert into DPClient (CL_Referer, CL_Days, CL_FK_DPStorage) values (?, ?, ?)");
			l_pstmt.setString(1, p_Referer);
			l_pstmt.setInt(2, p_Days);
			l_pstmt.setInt(3, l_pkStorage);
			l_pkClient = l_pstmt.executeUpdate();
		}

	}

	/**
	 * Remove a client of stored data
	 * 
	 * @param p_Datapoint
	 *            The concerned datapoint
	 * @param p_Referer
	 *            The id of the client
	 * @throws SQLException
	 */
	public void removeStorage(Datapoint p_Datapoint, String p_Referer)
			throws SQLException {
		// Check if datapoint already registered for storage
		PreparedStatement l_pstmt;

		l_pstmt = m_db
				.prepareStatement("select DPS_PK_DPStorage from DPStorage where DPS_GroupAddress = ? and DPS_DPT = ?");
		l_pstmt.setInt(1, p_Datapoint.getMainAddress().getRawAddress());
		l_pstmt.setString(2, p_Datapoint.getDPT());
		ResultSet l_rs = l_pstmt.executeQuery();

		int l_pkStorage = -1;

		if (l_rs.next())
			l_pkStorage = l_rs.getInt(1);

		l_rs.close();

		if (l_pkStorage == -1)
			return;

		// delete client
		l_pstmt = m_db
				.prepareStatement("delete from DPClient where CL_Referer = ? and CL_FK_DPStorage = ?");
		l_pstmt.setString(1, p_Referer);
		l_pstmt.setInt(2, l_pkStorage);
		l_pstmt.executeUpdate();

		// check if other clients
		l_pstmt = m_db
				.prepareStatement("select CL_PK_Client from DPClient where CL_FK_DPStorage = ?");
		l_pstmt.setInt(1, l_pkStorage);
		l_rs = l_pstmt.executeQuery();		

		if (!l_rs.next()) {
			l_rs.close();
			//delete data and storage
			deleteData(l_pkStorage);
			l_pstmt = m_db
					.prepareStatement("delete from DPStorage where DPS_PK_DPStorage = ?");
			l_pstmt.setInt(1, l_pkStorage);
			l_pstmt.executeUpdate();
		} else {
			l_rs.close();
			shrinkData(l_pkStorage);
		}
	}

	private void deleteData(int p_pkStorage) throws SQLException {
		PreparedStatement l_pstmt = m_db
				.prepareStatement("delete from DPValue where DPD_FK_DPStorage = ?");
		l_pstmt.setInt(1, p_pkStorage);
		l_pstmt.executeUpdate();
	}
	
	private void shrinkData(int p_pkStorage) throws SQLException {
		// select max days
		PreparedStatement l_pstmt = m_db
				.prepareStatement("select max(CL_Days) from DPClient where CL_FK_DPStorage = ?");
		l_pstmt.setInt(1, p_pkStorage);
		ResultSet l_rs = l_pstmt.executeQuery();

		int l_MaxDays = -1;
		if (l_rs.next())
			l_MaxDays = l_rs.getInt(1);

		l_rs.close();

		if (l_MaxDays != -1) {
			// shrink data according to max days
			l_pstmt = m_db
					.prepareStatement("delete from DPValue where DPD_FK_DPStorage = ? and DPD_Date < ?");
			l_pstmt.setInt(1, p_pkStorage);
			Calendar l_cal = Calendar.getInstance();
			l_cal.setTime(new Date());
			l_cal.add(Calendar.DATE, l_MaxDays * -1);
			l_pstmt.setDate(2, new java.sql.Date(l_cal.getTime().getTime()));
			l_pstmt.executeUpdate();
		} else {
			// shrink data according to average days
			l_pstmt = m_db
					.prepareStatement("select avg(CL_Days) from DPClient");
			l_rs = l_pstmt.executeQuery();

			int l_avgDays = -1;

			if (l_rs.next())
				l_avgDays = l_rs.getInt(1);

			l_rs.close();

			l_pstmt = m_db
					.prepareStatement("delete from DPValue where DPD_FK_DPStorage = ? and DPD_Date < ?");
			l_pstmt.setInt(1, p_pkStorage);
			Calendar l_cal = Calendar.getInstance();
			l_cal.setTime(new Date());
			l_cal.add(Calendar.DATE, l_avgDays * -1);
			l_pstmt.setDate(2, new java.sql.Date(l_cal.getTime().getTime()));
			l_pstmt.executeUpdate();
		}

	}

	/**
	 * Get the data for a time range
	 * 
	 * @param p_Datapoint
	 *            The datapoint concerned
	 * @param p_Start
	 *            The start date (most recent)
	 * @param p_End
	 *            The end date
	 * @return ArrayList of storage data
	 * @throws Exception
	 */
	public ArrayList<StorageData> getStorage(Datapoint p_Datapoint,
			Date p_Start, Date p_End) throws Exception {
		ArrayList<StorageData> l_results = new ArrayList<StorageData>();
		PreparedStatement l_pstmt;
		l_pstmt = m_db
				.prepareStatement("select DPS_PK_DPStorage from DPStorage where DPS_GroupAddress = ? and DPS_DPT = ?");
		l_pstmt.setInt(1, p_Datapoint.getMainAddress().getRawAddress());
		l_pstmt.setString(2, p_Datapoint.getDPT());
		ResultSet l_rs = l_pstmt.executeQuery();

		int l_pkStorage = -1;

		if (l_rs.next())
			l_pkStorage = l_rs.getInt(1);

		l_rs.close();

		if (l_pkStorage == -1)
			throw new Exception("No storage for this datapoint");

		l_pstmt = m_db
				.prepareStatement("select DPD_Value, DPD_Date from DPValue where DPD_FK_DPStorage = ? and DPD_Date between ? and ?");
		l_pstmt.setInt(1, l_pkStorage);
		l_pstmt.setTimestamp(2, new java.sql.Timestamp(p_Start.getTime()));
		l_pstmt.setTimestamp(3, new java.sql.Timestamp(p_End.getTime()));

		l_rs = l_pstmt.executeQuery();

		while (l_rs.next())
			l_results.add(new StorageData(l_rs.getString(1), l_rs
					.getTimestamp(2)));

		l_rs.close();

		updateLastRead(l_pkStorage);

		return l_results;
	}

	/**
	 * Get the last X days of data
	 * 
	 * @param p_Datapoint
	 *            The datapoint concerned
	 * @param p_Days
	 *            The number of days
	 * @return ArrayList of storage data
	 * @throws Exception
	 */
	public ArrayList<StorageData> getLastStorage(Datapoint p_Datapoint,
			int p_Days) throws Exception {
		Calendar l_cal = Calendar.getInstance();
		Date l_now = new Date();
		l_cal.setTime(l_now);
		l_cal.add(Calendar.DATE, p_Days * -1);
		return getStorage(p_Datapoint, l_cal.getTime(), l_now);
	}
	
	/**
	 * List the groups that have storage activated
	 * @return The group numbers
	 * @throws SQLException 
	 */
	public ArrayList<Integer> getStorageGroups() throws SQLException{
		ArrayList<Integer> l_groups = new ArrayList<Integer>();
		PreparedStatement l_pstmt = m_db.prepareStatement("select distinct DPS_GroupAddress from DPStorage inner join DPClient on CL_FK_DPStorage = DPS_PK_DPStorage");
		ResultSet l_rs = l_pstmt.executeQuery();
		
		while(l_rs.next()){
			l_groups.add(l_rs.getInt(1));
		}
		l_rs.close();
		
		return l_groups;
	}
	
	/**
	 * Get a list of datapoints by group address
	 * @param p_group The group address
	 * @return The list of datapoints
	 * @throws SQLException
	 */
	public ArrayList<ch.eiafr.web.knx.admin.Datapoint> getDPByGroup(int p_group) throws SQLException{
		ArrayList<ch.eiafr.web.knx.admin.Datapoint> l_DPs = new ArrayList<ch.eiafr.web.knx.admin.Datapoint>();
		PreparedStatement l_pstmt = m_db.prepareStatement("select DPS_DPName, DPS_DPT, DPS_LastRead from DPStorage where DPS_GroupAddress = ?");
		l_pstmt.setInt(1, p_group);
		ResultSet l_rs = l_pstmt.executeQuery();
		
		while(l_rs.next()){
			ch.eiafr.web.knx.admin.Datapoint l_DP = new ch.eiafr.web.knx.admin.Datapoint();
			l_DP.setName(l_rs.getString(1));
			l_DP.setNumber(l_rs.getString(2));
			l_DP.setLastRead(l_rs.getTimestamp(3));
			l_DPs.add(l_DP);
		}
		l_rs.close();
		
		return l_DPs;
	}
	
	/**
	 * Get subscribers of a datapoint
	 * @param p_Group The group address
	 * @param p_Number The datapoint number
	 * @return A list of subscribers
	 * @throws SQLException 
	 */
	public ArrayList<Subscriber> getSubscribers(int p_Group, String p_Number) throws SQLException {
		ArrayList<Subscriber> l_subs = new ArrayList<Subscriber>();
		PreparedStatement l_pstmt = m_db.prepareStatement("select CL_Referer, CL_Days from DPClient inner join DPStorage on CL_FK_DPStorage = DPS_PK_DPStorage where DPS_GroupAddress = ? and DPS_DPT = ?");
		l_pstmt.setInt(1, p_Group);
		l_pstmt.setString(2, p_Number);
		ResultSet l_rs = l_pstmt.executeQuery();
		
		while(l_rs.next()){
			Subscriber l_sub = new Subscriber(l_rs.getString(1), l_rs.getInt(2));
			l_subs.add(l_sub);
		}
		l_rs.close();
		
		return l_subs;
	}

	private void updateLastRead(int p_PkStorage) throws SQLException {
		PreparedStatement l_pstmt = m_db
				.prepareStatement("update DPStorage set DPS_LastRead=now() where DPS_PK_DPStorage = ?");
		l_pstmt.setInt(1, p_PkStorage);
		l_pstmt.executeUpdate();
	}

	@Override
	public void update(Observable o, Object arg) {
		ProcessEvent l_evt = (ProcessEvent) arg;

		try {
			// get all datapoints associated with group address
			PreparedStatement l_pstmt = m_db
					.prepareStatement("select DPS_PK_DPStorage, DPS_DPT from DPStorage where DPS_GroupAddress = ?");
			l_pstmt.setInt(1, l_evt.getDestination().getRawAddress());
			ResultSet l_rs = l_pstmt.executeQuery();

			HashMap<Integer, String> l_dps = new HashMap<Integer, String>();

			while (l_rs.next()) {
				l_dps.put(l_rs.getInt(1), l_rs.getString(2));
			}
			l_rs.close();
			
			Iterator<Entry<Integer, String>> l_it = l_dps.entrySet().iterator();

			while (l_it.hasNext()) {
				Map.Entry<Integer, String> l_entry = l_it.next();
				DPTXlator l_translator;
				try {
					String l_dpt = l_entry.getValue();
					l_translator = TranslatorTypes.createTranslator(
							Integer.parseInt(l_dpt.substring(0, l_dpt.indexOf("."))), l_dpt);
					l_translator.setData(l_evt.getASDU());

					// store data
					l_pstmt = m_db
							.prepareStatement("insert into DPValue (DPD_FK_DPStorage, DPD_Value, DPD_Date) values (?, ?, now())");
					l_pstmt.setInt(1, l_entry.getKey());
					l_pstmt.setString(2, l_translator.getValue());
					l_translator.getValue();
					l_pstmt.executeUpdate();

					break;
				} catch (KNXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			logger.error("Error in storing value for datapoint " + e.toString());
		}
	}
}
