package com.advicetec.configuration;

import java.net.InetAddress;

import org.codehaus.jackson.annotate.JsonProperty;

public class DisplayDevice extends ConfigurationObject  
{

	@JsonProperty("reference_cd") 
	String referenceCd;
    
	@JsonProperty("display_type") 
    DisplayType displayType;
    
	@JsonProperty("descr") 
    String descr;
    
	@JsonProperty("ip_address") 
    InetAddress ipAddress;
    
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

	
}
