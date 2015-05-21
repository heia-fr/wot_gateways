package ch.eiafr.web.enocean.admin;

import java.util.Date;

public class Data {
	private int m_id;
	private float m_value;
	private Date m_date;
    private int m_measure;

	public Data() {
	}

	public Data(int p_id, float p_value, Date p_date, int p_measure) {
		this.m_id = p_id;
		this.m_value = p_value;
        this.m_date = p_date;
        this.m_measure = p_measure;
	}

	public int getId() {
		return m_id;
	}

	public void setId(int p_id) {
		this.m_id = p_id;
	}

	public float getValue() {
		return m_value;
	}

	public void setName(float p_value) {
		this.m_value = p_value;
	}

	public Date getDate() {
		return m_date;
	}

	public void setDate(Date p_date) {
		this.m_date = p_date;
	}

    public int getMeasure() {
        return m_measure;
    }

    public void setMeasure(int p_measure) {
        this.m_measure = p_measure;
    }


	@Override
	public String toString() {
		return "Data [m_id=" + m_id + ", m_value=" + m_value
				+ ", m_date=" + m_date + ", m_measure=" + m_measure + "]";
	}
}
