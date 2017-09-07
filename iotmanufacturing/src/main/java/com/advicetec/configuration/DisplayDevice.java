package com.advicetec.configuration;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Display Device Object, it represents a display entity. 
 * @author Andres Marentes
 *
 */
public class DisplayDevice extends ConfigurationObject  
{

	static Logger logger = LogManager.getLogger(DisplayDevice.class.getName());
	
	/**
	 * Alternative identifier for the display device.
	 */
	@JsonProperty("reference_cd") 
	String referenceCd;
    
	/**
	 * Identifies the configuration of this display device.
	 */
	@JsonProperty("display_type") 
    DisplayType displayType;
    
	/**
	 * Description of the display device.
	 */
	@JsonProperty("descr") 
    String descr;
    
	/**
	 * Ip Address where the display is listening to connections
	 */
	@JsonProperty("ip_address") 
    InetAddress ipAddress;
    
	/**
	 * port where the display is listening to connections
	 */
	@JsonProperty("port") 
    Integer port;

	
	/**
	 * Constructor for the class 
	 * @param id  identifier given in the configuration database.
	 */
	public DisplayDevice(Integer id) {
		super(id);
	}


	/**
	 * Gets the alternative identifier. 
	 * @return
	 */
	public String getReferenceCd() {
		return referenceCd;
	}


	/**
	 * Sets the alternative identifier for the display device.
	 * @param referenceCd  alternative identifier 
	 */
	public void setReferenceCd(String referenceCd) {
		this.referenceCd = referenceCd;
	}


	/**
	 * Gets the display type, which is used to configure display options.
	 * @return
	 */
	public DisplayType getDisplayType() {
		return displayType;
	}


	/**
	 * Sets the display type, which is used to configure display options.
	 * @param displayType  Identifier of the display type to use.
	 */
	public void setDisplayType(DisplayType displayType) {
		this.displayType = displayType;
	}

	/**
	 * Gets the description of the display device.
	 * @return Display device description 
	 */
	public String getDescr() {
		return descr;
	}

	/**
	 * Sets the description of the display device.
	 * @param descr description to set.
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}

	/**
	 * Gets the ip address where the display device is listening to 
	 * @return  Listening Ip Address 
	 */
	public InetAddress getIpAddress() {
		return ipAddress;
	}

	/**
	 * Sets the Ip address where the display device is listening to 
	 * @param ipAddress Listening Ip Address
	 */
	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * Gets the port where the display device is listening to
	 * @return listening port.
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * Sets the port where the display device is listening to
	 * @param port listening port.
	 */
	public void setPort(Integer port) {
		this.port = port;
	}

	/**
	 * Method to serialize the display device into JSON.
	 * @return json object representation.
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
