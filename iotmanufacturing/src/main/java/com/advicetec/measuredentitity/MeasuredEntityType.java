package com.advicetec.measuredentitity;

import java.io.IOException;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * This enumeration describes possible types of business entities 
 * able to be measured.
 * 
 * @author advicetec
 *
 */
public enum MeasuredEntityType 
{

	/**
	 * Machine type 
	 */
	MACHINE(0,"Machine"),
	
	/**
	 * Plants  
	 */
	PLANT(1,"Plant"),
	
	/**
	 * Facility 
	 */
	FACILITY(2,"Facility"),
	
	/**
	 * Company  
	 */
	COMPANY(3,"Company"),
	
	/**
	 * Job 
	 */
	JOB(4,"Job"),
	
	/**
	 * Underfinedc
	 */
	UNDEFINED(4,"Undefined");
	
	/**
	 * code of the measured entity type. 
	 */
	@JsonProperty("value")
	private int code;
	
	/**
	 * Name of the measured entity type.
	 */
	@JsonProperty("name")
	private String name;

	/**
	 * Measured entity type constructor
	 * 
	 * @param value  code of the measured entity type
	 * @param name	 name of the measured entity type 
	 */
	@JsonCreator
	private MeasuredEntityType(
			@JsonProperty("value")int value, 
			@JsonProperty("name")String name) {
		this.code = value;
		this.name = name;
	}

	/**
	 * Gets the code of the measured entity type 
	 * 
	 * @return	measured entity type
	 */
	public int getValue() {
		return this.code;
	}

	/**
	 * Gets the name of the measured entity type
	 * @return	name 
	 */
	public String getName() {
		return this.name;
	}	

	/**
	 * Gets a Json representation of the measured entity type.
	 * 
	 * @return  Json representation.
	 */
	public String toJson(){
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			System.err.println("Cannot serialize the AttributeOrigin object.");
			e.printStackTrace();
		}
		return json;
	}

}
