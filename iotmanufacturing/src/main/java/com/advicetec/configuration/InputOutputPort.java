package com.advicetec.configuration;

/**
 * 
 * @author user
 *
 */
public class InputOutputPort extends ConfigurationObject  {
	
	private Signal signal_type;
	private String	transformation_text;
	
	public InputOutputPort(Integer id) {
		super(id);
	}

	public Signal getSignalType() {
		return signal_type;
	}

	public void setSignalType(Signal signal_type) {
		this.signal_type = signal_type;
	}

	public String getTransformationText() {
		return transformation_text;
	}

	public void setTransformationText(String transformation_text) {
		this.transformation_text = transformation_text;
	}

	
}
