package ch.eiafr.enocean.eep;

public class FieldConversion{
	private double validRangeMax = -1;
	private double validRangeMin = -1;
	private double scaleMax = -1;
	private double scaleMin = -1;
	private String unit;
	
	public FieldConversion(double validRangeMax, double validRangeMin, double scaleMax, double scaleMin, String unit){
		this.validRangeMax = validRangeMax;
		this.validRangeMin = validRangeMin;
		this.scaleMax = scaleMax;
		this.scaleMin = scaleMin;
		this.unit = unit;
	}
	
	public FieldConversion() {
		
	}

	public double getValidRangeMax() {
		return validRangeMax;
	}

	public double getValidRangeMin() {
		return validRangeMin;
	}

	public double getScaleMax() {
		return scaleMax;
	}

	public double getScaleMin() {
		return scaleMin;
	}

	public String getUnit() {
		return unit;
	}

	public void setValidRangeMax(double validRangeMax) {
		this.validRangeMax = validRangeMax;
	}

	public void setValidRangeMin(double validRangeMin) {
		this.validRangeMin = validRangeMin;
	}

	public void setScaleMax(double scaleMax) {
		this.scaleMax = scaleMax;
	}

	public void setScaleMin(double scaleMin) {
		this.scaleMin = scaleMin;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
}
