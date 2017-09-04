package com.advicetec.configuration;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * This class represents an input or output port from a measuring device. 
 * 
 * Logically, there should be a one to one relationship with a sensor receiving or sending information.
 * 
 * In this class we configure the measured entity being monitored, the transformation used to convert 
 * the signal into attributes meaningful for the monitored entity. 
 * 
 * @author Andres Marentes
 *
 */
public class InputOutputPort extends ConfigurationObject  {
	
	static Logger logger = LogManager.getLogger(InputOutputPort.class.getName());
	
	/**
	 *   Reference to the signal type configured for this input output port. 
	 */
	@JsonProperty("signal_type")
	private Signal signalType;
	
	/**
	 * Transformation text.
	 */
	@JsonProperty("transformation_text") 
	private String transformationText;
	
	/**
	 * Identifier for identifying and connecting to the corresponding sensor. 
	 * This value has multiple meaning according to the protocol used for the communication.
	 */
	@JsonProperty("port_label")
	private String portLabel;

	/**
	 * Measured entity that is the target of the signal. This value is the identifier of that entity.
	 */
	@JsonProperty("measured_entity")
	private Integer measuringEntity; 
	
	/**
	 * In case of modbus, this property defines how often a new reading or writing should be performed.
	 */
	@JsonProperty("refresh_time_ms")
	private Integer refreshTimeMs; 
	
	/**
	 * Constuctor for the class, it receives the  identifier for the input output port.
	 * @param id  Identifier for the instance.
	 */
	@JsonCreator
	public InputOutputPort(@JsonProperty("id") Integer id) {
		super(id);
		refreshTimeMs = new Integer(0);
	}	

	/**
	 * Gets the refresh time configured in case of modbus
	 * @return  Refresh time or zero in case of not defined.
	 */
	public Integer getRefreshTimeMs() {
		return refreshTimeMs;
	}

	/**
	 * Sets refresh time to be used for modbus connections.
	 * 
	 * @param refreshTimeMs  Refresh time in milliseconds.
	 */
	public void setRefreshTimeMs(Integer refreshTimeMs) {
		this.refreshTimeMs = refreshTimeMs;
	}

	/**
	 * Gets the port label 
	 * @return port label
	 */
	public String getPortLabel() {
		return portLabel;
	}

	/**
	 * Sets the port label 
	 * @param portLabel  port label
	 */
	public void setPortLabel(String portLabel) {
		this.portLabel = portLabel;
	}

	/**
	 * Gets the signal type configured
	 * @return  signal type object.
	 */
	public Signal getSignalType() {
		return signalType;
	}

	/**
	 * Sets the signal type 
	 * @param signal_type signal type.
	 */
	public void setSignalType(Signal signal_type) {
		this.signalType = signal_type;
	}

	/**
	 * Gets the transformation text 
	 * @return transformation text
	 */
	public String getTransformationText() {
		return transformationText;
	}

	/**
	 * Sets the transformation text
	 * 
	 * @param transformation_text  transformation text
	 */
	public void setTransformationText(String transformation_text) {
		this.transformationText = transformation_text;
	}

	/**
	 * Gets the identifier of the measured entity configured
	 * @return  measured entity identifier
	 */
	public Integer getMeasuringEntity() {
		return measuringEntity;
	}

	/**
	 * Sets the identifier of the measured entity 
	 * @param measuringEntity  identifier of the measured entity
	 */
	public void setMeasuringEntity(Integer measuringEntity) {
		this.measuringEntity = measuringEntity;
	}

	/**
	 * Serialize the input output port to JSON 
	 * @return JSON representation.
	 */
	public String toJson()
	{
		ObjectMapper mapper = new ObjectMapper();
			
		String jsonInString=null;
		try {
			
			jsonInString = mapper.writeValueAsString(this);
			
			
		} catch (JsonGenerationException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return jsonInString;
	}	
	
}
