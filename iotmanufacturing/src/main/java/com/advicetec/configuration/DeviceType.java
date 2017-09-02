package com.advicetec.configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;

/**
 * @author Andres Marentes
 *
 *  This class represents all device types used in the system. 
 *
 */
public class DeviceType extends ConfigurationObject
{
	
	static Logger logger = LogManager.getLogger(DeviceType.class.getName());
	
	/**
	 * Device type description.
	 */
	@JsonProperty("descr") 
	private String descr;
	
	/**
	 * Device type creation date
	 */
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
    private LocalDateTime create_date;

	
	/**
	 * Input output signals defined for the device type.
	 */
	@JsonProperty("io_signals")
	protected List<IOSignalDeviceType> signals;
    
	/**
	 * Constructor for a device type 
	 * @param id  identifier of this configurable object.
	 */
	@JsonCreator
	public DeviceType(@JsonProperty("id") Integer id) {
		super(id);
		signals = new ArrayList<IOSignalDeviceType>();
	}
	
	/**
	 * @return Gets the device type description
	 */
	public String getDescr() {
		return descr;
	}
	
	/**
	 * Sets the device type description
	 * @param descr  description to set.
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}
	
	/**
	 * Gets the creation date 
	 * @return   Creation date 
	 */
	public LocalDateTime getCreate_date() {
		return create_date;
	}
	
	/**
	 * Sets the creation date
	 * @param create_date 
	 */
	public void setCreate_date(LocalDateTime create_date) {
		this.create_date = create_date;
	}  
    
	/**
	 * Includes a new input output signal in the device type.
	 * 
	 * @param signal input/output signal to include.
	 */
	public void putIOSignal(IOSignalDeviceType signal)
	{
		this.signals.add(signal);
	}
	
	/**
	 * Get the input/output signal from its identifier.
	 * @param signalId  Input/output signal identifier.
	 * @return Input/Output Signal object.
	 */
	public IOSignalDeviceType getIOSignal(Integer signalId)
	{
		for (int i = 0; i < this.signals.size(); i++){
			if (this.signals.get(i).getId().equals(signalId)){
				return this.signals.get(i);
			}
		}
		
		return null;
	}
	
	/**
	 * @return Json object representing the device type
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
	
	/**
	 * @return the number of input/output signals associated to the device type.
	 */
	@JsonIgnore
	public int getNumberSignals(){
		return this.signals.size();
	}
	
}
