package com.advicetec.measuredentitity;

import java.io.IOException;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

public enum MeasuredEntityScheduledEventType 
{

	
	OEE(0,"OEE Aggregation"),
	UNDEFINED(1,"Undefined");
	
	@JsonProperty("value")
	private int code;
	@JsonProperty("name")
	private String name;

	@JsonCreator
	private MeasuredEntityScheduledEventType(
			@JsonProperty("value")int value, 
			@JsonProperty("name")String name) {
		this.code = value;
		this.name = name;
	}

	public int getValue() {
		return this.code;
	}

	public String getName() {
		return this.name;
	}	

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
