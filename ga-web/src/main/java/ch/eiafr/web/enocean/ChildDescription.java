package ch.eiafr.web.enocean;

public class ChildDescription {

	private String name;
	private boolean isSensor;
	
	
	public ChildDescription(String name, boolean isSensor) {
		this.name = name;
		this.isSensor = isSensor;
	}
	public String getName() {
		return name;
	}
	public boolean isSensor() {
		return isSensor;
	}
	

}
