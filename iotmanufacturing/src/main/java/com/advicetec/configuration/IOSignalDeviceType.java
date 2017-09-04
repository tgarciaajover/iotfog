package com.advicetec.configuration;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * This class maintains the configuration information for a Input Output Signal. 
 * 
 * In this class, the user configures the signal protocol and its direction (input or output).
 * 
 * @author Andres Marentes
 *
 */
public class IOSignalDeviceType extends ConfigurationObject
{
	
	static Logger logger = LogManager.getLogger(IOSignalDeviceType.class.getName());

	/**
	 * Signal being used for this input output signal.
	 */
	private Signal signal;
    
	/**
	 *  Communication direction: I - Input, O - Output.  
	 */
	@JsonProperty("i_o") 
	private String I_O;
    
	/**
	 * Constructor for the I/O Signal device type.
	 * @param id identifier of the Signal Device Type
	 */
	public IOSignalDeviceType(@JsonProperty("id") Integer id) {
		super(id);
	}
	
	/**
	 * Gets the signal configured
	 * @return Signal instance
	 */
	public Signal getSignal() {
		return signal;
	}
	
	/**
	 * Sets the signal of this I/O signal device type
	 * @param signal signal to set.
	 */
	public void setSignal(Signal signal) {
		this.signal = signal;
	}
	
	/**
	 * Gets the direction of the communication
	 * 
	 * @return  Input / Output direction.
	 */
	public String getI_O() {
		return I_O;
	}
	
	/**
	 * Sets the direction or the communication
	 * 
	 * @param i_O  Input or Output
	 */
	public void setI_O(String i_O) {
		I_O = i_O;
	}
    
	/**
	 * Object's serialization to JSON. 
	 * @return  JSON serialization
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
