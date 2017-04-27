package com.advicetec.measuredentitity;

import java.io.IOException;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

public class ReasonCode 
{
	@JsonProperty
	String id;
	@JsonProperty
	String description;

	public ReasonCode(
			@JsonProperty("id")String id, 
			@JsonProperty("description")String description) {
		super();
		this.id = id;
		this.description = description;
	}
	
	public String getId() {
		return id;
	}
		
	public String getDescription() {
		return description;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("id:").append(id).append(",");
		sb.append("description:").append(description);
		return sb.toString();
	}

	public String toJson() {
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json;
	}
}
