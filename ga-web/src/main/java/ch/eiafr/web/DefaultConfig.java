package ch.eiafr.web;

/**
 * Default value when the properties file "ga-logger" is not found
 * 
 * @author Sebastien Baudin
 * 
 */
public class DefaultConfig {
	// Not in the config file
	public static final String PROPERTIES_FILE = "greenapp.properties";
	public static final String DATABASE_DRIVER = "com.mysql.jdbc.Driver";

	// Must be in the config file "ga-logger.properties"
	public static final String DB_URL = "jdbc:mysql://localhost:3306/greenapp";
	public static final String DB_USER = "root";
	public static final String DB_PASSWORD = "root";
	public static final boolean RESET_KEY = false;
	public static final String SERIAL_PORT = "/dev/ttyUSB0";
	public static final int DISCOVER_TIME = 20;
	public static final int REQUEST_TIME = 10;

	public static final String USE_PLOGG = "true";
	public static final String USE_RIDEO = "true";

	public static final String IPS_PROPERTIES = "ips-logger.properties";
	public static final String IP_ADDRESS = "160.98.21.236";
	public static final String PORT = "161";
	/**
	 * The Snmp versions are: <br>
	 * <b>version1 = 0</b> <br>
	 * <b>version2c = 1</b> <br>
	 * <b>version3 = 3 </b>
	 */
	public static final String SNMP_VERSION = "1"; // 1 stands for
													// Snmp.version2c
	public static final String COMMUNITY = "public";
	public static final String ENERGY_REQUEST = "30";
	public static final String POWER_REQUEST = "10";
	public static final String ERROR_DEBUG = "false";
}
