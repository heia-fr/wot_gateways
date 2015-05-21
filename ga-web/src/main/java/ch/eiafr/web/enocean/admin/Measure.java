package ch.eiafr.web.enocean.admin;

public class Measure {
	private int m_id;
	private String m_unit;
	private float m_scaleMax;
	private float m_scaleMin;
	private String m_eepShorcut;
	

	public Measure() {
	}

	public Measure(int p_id, String p_unit, float p_scaleMax, float p_scaleMin,
			String p_eepShorcut) {
		this.m_id = p_id;
		this.m_unit = p_unit;
		this.m_scaleMax = p_scaleMax;
		this.m_scaleMin = p_scaleMin;
		this.m_eepShorcut = p_eepShorcut;
	}
	
	public Measure(String p_unit, float p_scaleMax, float p_scaleMin,
            String p_eepShorcut) {
        this.m_unit = p_unit;
        this.m_scaleMax = p_scaleMax;
        this.m_scaleMin = p_scaleMin;
        this.m_eepShorcut = p_eepShorcut;
    }

	public int getId() {
		return m_id;
	}

	public void setId(int p_id) {
		this.m_id = p_id;
	}

	public void setUnit(String p_unit) {
		this.m_unit = p_unit;
	}

	public String getUnit() {
		return m_unit;
	}

	public float getScaleMax() {
		return m_scaleMax;
	}

	public void setScaleMax(float p_scaleMax) {
		this.m_scaleMax = p_scaleMax;
	}

	public float getScaleMin() {
		return m_scaleMin;
	}

	public void setScaleMin(float p_scaleMin) {
		this.m_scaleMin = p_scaleMin;
	}

	public String getEepShortcut() {
		return m_eepShorcut;
	}

	public void setEepShorcut(String p_eepShorcut) {
		this.m_eepShorcut = p_eepShorcut;
	}
}
