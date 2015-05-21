package ch.eiafr.web.enocean;

public class SensorEEP {

	int IdSensor, RORG, function, type;

	public SensorEEP(int IdSensor, int RORG, int function, int type) {
		this.IdSensor = IdSensor;
		this.RORG = RORG;
		this.function = function;
		this.type = type;
	}

	public int getRORG() {
		return RORG;
	}

	public int getFunction() {
		return function;
	}

	public int getType() {
		return type;
	}

	public int getIdSensor() {
		return IdSensor;
	}

}
