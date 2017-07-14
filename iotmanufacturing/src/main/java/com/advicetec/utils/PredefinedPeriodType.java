package com.advicetec.utils;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import com.advicetec.applicationAdapter.ProductionOrderManager;

public enum PredefinedPeriodType 
{
	
	YEAR(0,"Year"),
	MONTH(1,"Month"),
	DAY(2,"Day"),
	HOUR(3,"Hour"),
	UNDEFINED(4,"Undefined");
	
	@JsonProperty("value")
	private int code;
	@JsonProperty("name")
	private String name;

	@JsonCreator
	private PredefinedPeriodType(
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
			Logger logger = LogManager.getLogger(PredefinedPeriodType.class.getName());
			logger.error("Cannot serialize the predefined Period Type object.");
		}
		return json;
	}


}
