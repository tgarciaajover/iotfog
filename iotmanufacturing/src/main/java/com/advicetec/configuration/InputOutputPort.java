package com.advicetec.configuration;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * 
 * @author user
 *
 */
public class InputOutputPort extends ConfigurationObject  {
	
	
	@JsonProperty("signal_type")
	private Signal signalType;
	
	@JsonProperty("transformation_text") 
	private String transformationText;
	
	@JsonProperty("port_label")
	private String portLabel;

	@JsonProperty("measured_entity")
	private String measuringEntity; 
	
	@JsonCreator
	public InputOutputPort(@JsonProperty("id") Integer id) {
		super(id);
	}	

	public String getPortLabel() {
		return portLabel;
	}

	public void setPortLabel(String portLabel) {
		this.portLabel = portLabel;
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

	public String getMeasuringEntity() {
		return measuringEntity;
	}

	public void setMeasuringEntity(String measuringEntity) {
		this.measuringEntity = measuringEntity;
	}

	public String toJson()
	{
		ObjectMapper mapper = new ObjectMapper();
			
		String jsonInString=null;
		try {
			
			jsonInString = mapper.writeValueAsString(this);
			
			
		} catch (JsonGenerationException e) {
			// TODO: log the error
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return jsonInString;
	}	
	
}
