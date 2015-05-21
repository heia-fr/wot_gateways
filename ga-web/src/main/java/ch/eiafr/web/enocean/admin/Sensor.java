package ch.eiafr.web.enocean.admin;

import java.util.ArrayList;
import java.util.Date;



public class Sensor {
	private int m_id;
	private String m_name;
	private String m_description;
	private int m_locationId;
	private String m_locationName;
	private String m_locationPath;
	private String m_locationTypeName;
	private String m_locationTypeImgUrl;
	private String m_manufacturer;
	private int m_eepRorg;
	private int m_eepFunction;
	private int m_eepType;
	private String m_lastModifier;
	private Date m_lastModification;
	private boolean m_actuator;
	private boolean m_hybridMode;
	private int m_address;
	private ArrayList<Measure> m_measure;

	public Sensor() {
	}

	public Sensor(int p_id, String p_name, String p_description,
			int p_locationId, String p_locationPath, String p_locationTypeName,
			String p_locationTypeImgUrl, String p_manufacturer, int p_eepRorg,
			int p_eepFunction, int p_eepType, String p_lastModifier,
			Date p_lastModification, boolean p_actuator, boolean p_hybridMode,
			int p_address, ArrayList<Measure> p_measure) {
		this.m_id = p_id;
		this.m_name = p_name;
		this.m_description = p_description;
		this.m_locationId = p_locationId;
		this.m_locationPath = p_locationPath;
		this.m_locationTypeName = p_locationTypeName;
		this.m_locationTypeImgUrl = p_locationTypeImgUrl;
		this.m_manufacturer = p_manufacturer;
		this.m_eepRorg = p_eepRorg;
		this.m_eepFunction = p_eepFunction;
		this.m_eepType = p_eepType;
		this.m_lastModifier = p_lastModifier;
		this.m_lastModification = p_lastModification;
		this.m_actuator = p_actuator;
		this.m_hybridMode = p_hybridMode;
		this.m_address = p_address;
		this.m_measure = p_measure;
	}

	// constructor for the sensor POST request
	public Sensor(int p_id, String p_name, String p_description,
			int p_locationId, String p_locationPath, String p_locationTypeName,
			String p_locationTypeImgUrl, String p_manufacturer, int p_eepRorg,
			int p_eepFunction, int p_eepType, String p_lastModifier,
			Date p_lastModification, boolean p_actuator, boolean p_hybridMode,
			int p_address) {
		this.m_id = p_id;
		this.m_name = p_name;
		this.m_description = p_description;
		this.m_locationId = p_locationId;
		this.m_locationPath = p_locationPath;
		this.m_locationTypeName = p_locationTypeName;
		this.m_locationTypeImgUrl = p_locationTypeImgUrl;
		this.m_manufacturer = p_manufacturer;
		this.m_eepRorg = p_eepRorg;
		this.m_eepFunction = p_eepFunction;
		this.m_eepType = p_eepType;
		this.m_lastModifier = p_lastModifier;
		this.m_lastModification = p_lastModification;
		this.m_actuator = p_actuator;
		this.m_hybridMode = p_hybridMode;
		this.m_address = p_address;
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

	public String getDescription() {
		return m_description;
	}

	public void setDescription(String p_description) {
		this.m_description = p_description;
	}

	public int getLocationId() {
		return m_locationId;
	}

	public void setLocationId(int p_locationId) {
		this.m_locationId = p_locationId;
	}
	
	public String getLocationName() {
		return m_locationName;
	}

	public void setLocationName(String p_locationName) {
		this.m_locationName = p_locationName;
	}

	public String getLocationPath() {
		return m_locationPath;
	}

	public void setLocationPath(String p_locationPath) {
		this.m_locationPath = p_locationPath;
	}

	public String getLocationTypeName() {
		return m_locationTypeName;
	}

	public void setLocationTypeName(String p_locationType) {
		this.m_locationTypeName = p_locationType;
	}

	public String getLocationTypeImgUrl() {
		return m_locationTypeImgUrl;
	}

	public void setLocationTypeImgUrl(String p_locationTypeImgUrl) {
		this.m_locationTypeImgUrl = p_locationTypeImgUrl;
	}

	public String getManufacturer() {
		return m_manufacturer;
	}

	public void setManufacturer(String p_manufacturer) {
		this.m_manufacturer = p_manufacturer;
	}

	public int getEepRorg() {
		return m_eepRorg;
	}

	public void setEepRorg(int p_eepRorg) {
		this.m_eepRorg = p_eepRorg;
	}

	public int getEepFunction() {
		return m_eepFunction;
	}

	public void setEepFunction(int p_eepFunction) {
		this.m_eepFunction = p_eepFunction;
	}

	public int getEepType() {
		return m_eepType;
	}

	public void setEepType(int p_eepType) {
		this.m_eepType = p_eepType;
	}

	public String getLastModifier() {
		return m_lastModifier;
	}

	public void setLastModifier(String p_LastModifier) {
		this.m_lastModifier = p_LastModifier;
	}

	public Date getLastModification() {
		return m_lastModification;
	}

	public void setLastModification(Date p_LastModification) {
		this.m_lastModification = p_LastModification;
	}

	public boolean isActuator() {
		return m_actuator;
	}

	public void setActuator(boolean p_actuator) {
		this.m_actuator = p_actuator;
	}

	public boolean isHybridMode() {
		return m_hybridMode;
	}

	public void setHybridMode(boolean p_HybridMode) {
		this.m_hybridMode = p_HybridMode;
	}

	public int getAddress() {
		return m_address;
	}

	public void setAddress(int p_address) {
		this.m_address = p_address;
	}

	public ArrayList<Measure> getMeasure() {
		return m_measure;
	}

	public void setMeasure(ArrayList<Measure> m_measure) {
		this.m_measure = m_measure;
	}

	@Override
	public String toString() {
		return "Sensor [m_id=" + m_id + ", m_name=" + m_name
				+ ", m_description=" + m_description + ", m_locationPath="
				+ m_locationPath + ", m_locationTypeName=" + m_locationTypeName
				+ ", m_locationTypeImgUrl=" + m_locationTypeImgUrl
				+ ", m_manufacturer=" + m_manufacturer + ", m_eepRorg="
				+ m_eepRorg + ", m_eepFunction=" + m_eepFunction
				+ ", m_eepType=" + m_eepType + ", m_lastModifier="
				+ m_lastModifier + ", m_lastModification=" + m_lastModification
				+ ", m_actuator=" + m_actuator + ", m_hybridMode="
				+ m_hybridMode + ", m_address=" + m_address + ", m_measure="
				+ m_measure + "]";
	}
}
