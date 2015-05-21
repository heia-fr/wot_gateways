package ch.eiafr.web.enocean;

public class MeasureDescription {
	private String unit;
	private double scaleMax;
	private double scaleMin;
	private String shortcut;
	private int idMeasure;

	public MeasureDescription(int idMeasure, String unit, double scaleMax, double scaleMin,
			String shortcut) {
		this.idMeasure = idMeasure;
		this.unit = unit;
		this.scaleMax = scaleMax;
		this.scaleMin = scaleMin;
		this.shortcut = shortcut;
	}

	public int getIdMeasure() {
		return idMeasure;
	}

	public String getUnit() {
		return unit;
	}

	public double getScaleMax() {
		return scaleMax;
	}

	public double getScaleMin() {
		return scaleMin;
	}

	public String getShortcut() {
		return shortcut;
	}

}
