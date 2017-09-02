package com.advicetec.configuration;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class DisplayDevice extends ConfigurationObject  
{

	static Logger logger = LogManager.getLogger(DisplayDevice.class.getName());
	
	/**
	 * 
	 */
	@JsonProperty("reference_cd") 
	String referenceCd;
    
	/**
	 * 
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

	
	public DisplayDevice(Integer id) {
		super(id);
	}


	public String getReferenceCd() {
		return referenceCd;
	}


	public void setReferenceCd(String referenceCd) {
		this.referenceCd = referenceCd;
	}


	public DisplayType getDisplayType() {
		return displayType;
	}


	public void setDisplayType(DisplayType displayType) {
		this.displayType = displayType;
	}


	public String getDescr() {
		return descr;
	}


	public void setDescr(String descr) {
		this.descr = descr;
	}


	public InetAddress getIpAddress() {
		return ipAddress;
	}


	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}


	public Integer getPort() {
		return port;
	}


	public void setPort(Integer port) {
		this.port = port;
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
