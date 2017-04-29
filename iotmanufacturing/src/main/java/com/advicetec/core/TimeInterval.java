package com.advicetec.core;

import java.io.IOException;
import java.time.LocalDateTime;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;

public final class TimeInterval 
{
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonProperty("start")
	private LocalDateTime startDateTime;
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonProperty("end")
	private LocalDateTime endDateTime;
		
	@JsonCreator
	public TimeInterval(
			@JsonProperty("start")LocalDateTime startDateTime,
			@JsonProperty("end")LocalDateTime endDateTime) 
	{
		super();
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
	}

	public LocalDateTime getStart() 
	{
		return startDateTime;
	}

	public LocalDateTime getEnd() 
	{
		return endDateTime;
	}
	
	public String toJson(){
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			System.err.println("Cannot export this Interval as the json object.");
			e.printStackTrace();
		}
		return json;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("start: ").append(startDateTime.toString()).append(",");
		sb.append("end: ").append(endDateTime.toString());
		return sb.toString();
	}
}
