package com.advicetec.measuredentitity;

import java.io.IOException;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Measured entity scheduled event type 
 * 
 * @author advitec
 *
 */
public enum MeasuredEntityScheduledEventType 
{

	
	OEE(0,"OEE Aggregation"),
	UNDEFINED(1,"Undefined");
	
	/**
	 *  Measured entity scheduled event type code.  
	 */
	@JsonProperty("value")
	private int code;
	
	/**
	 * Measured entity scheduled event type code. 
	 */
	@JsonProperty("name")
	private String name;

	/**
	 * Constructor for the class 
	 * 
	 * @param value		code assigned to the type
	 * @param name		name given
	 */
	@JsonCreator
	private MeasuredEntityScheduledEventType(
			@JsonProperty("value")int value, 
			@JsonProperty("name")String name) {
		this.code = value;
		this.name = name;
	}

	/**
	 * Gets the code of this scheduled event type.
	 * 
	 * @return
	 */
	public int getValue() {
		return this.code;
	}

	/**
	 * Gets the name of this scheduled event type. 
	 * @return
	 */
	public String getName() {
		return this.name;
	}	

	/**
	 * Json serialization of this schedule event type.
	 * 
	 * @return
	 */
	public String toJson(){
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			System.err.println("Cannot serialize the Measured Entity Scheduled Event Object.");
			e.printStackTrace();
		}
		return json;
	}

}
