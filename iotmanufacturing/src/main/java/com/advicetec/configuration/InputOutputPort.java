package com.advicetec.configuration;

/**
 * 
 * @author user
 *
 */
public class InputOutputPort extends ConfigurationObject  {
	
	private Signal signalType;
	private String transformationText;
	private String behaviorText;
	private String portLabel;
	private String measuringEntity; 
	
	public String getPortLabel() {
		return portLabel;
	}

	public void setPortLabel(String portLabel) {
		this.portLabel = portLabel;
	}

	public InputOutputPort(Integer id) {
		super(id);
	}

	public Signal getSignalType() {
		return signalType;
	}

	public void setSignalType(Signal signal_type) {
		this.signalType = signal_type;
	}

	public String getTransformationText() {
		return transformationText;
	}

	public void setTransformationText(String transformation_text) {
		this.transformationText = transformation_text;
	}

	public String getBehaviorText() {
		return behaviorText;
	}

	public void setBehaviorText(String behaviorText) {
		this.behaviorText = behaviorText;
	}

	public String getMeasuringEntity() {
		return measuringEntity;
	}

	public void setMeasuringEntity(String measuringEntity) {
		this.measuringEntity = measuringEntity;
	}
	
}
