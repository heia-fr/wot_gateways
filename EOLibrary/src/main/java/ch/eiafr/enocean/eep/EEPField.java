package ch.eiafr.enocean.eep;

import java.util.ArrayList;
import java.util.Map;

/**
 * Represent a field of an EEP
 * 
 * @author gb
 * 
 */
public class EEPField {

	private int offset;
	private int size;
	private String data;
	private String shortcut;
	private String description;
	private boolean isStatus = false;
	private ArrayList<FieldConversion> fieldConversions;

	Map<Double, String> possibleValues;
	private int value = 0;

	public EEPField(int offset, int size, String data, String shortcut,
			String description, ArrayList<FieldConversion> fieldConversions,
			Map<Double, String> possibleValues, boolean isStatus, int value) {
		this.offset = offset;
		this.size = size;
		this.data = data;
		this.shortcut = shortcut;
		this.description = description;
		this.possibleValues = possibleValues;
		this.isStatus = isStatus;
		this.value = value;
		this.fieldConversions = fieldConversions;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public int getOffset() {
		return offset;
	}

	public int getSize() {
		return size;
	}

	public String getData() {
		return data;
	}

	public String getShortcut() {
		return shortcut;
	}

	public String getDescription() {
		return description;
	}

	public boolean isStatus() {
		return isStatus;
	}

	public Map<Double, String> getPossibleValues() {
		return possibleValues;
	}

	public boolean hasPossibleValues() {
		return possibleValues != null;
	}
	
	public ArrayList<FieldConversion> getFieldConversions() {
		return fieldConversions;
	}
}