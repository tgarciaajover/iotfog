package com.advicetec.core;

import java.io.IOException;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;

/**
 * This class represents a continuous range of time given by [from,to].
 * @author andres
 *
 */
public final class TimeInterval 
{
	
	static Logger logger = LogManager.getLogger(TimeInterval.class.getName());
	
	/**
	 * Start datetime of the interval
	 */
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonProperty("start")
	private LocalDateTime startDateTime;
	
	/**
	 * End datetime of the interval
	 */
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonProperty("end")
	private LocalDateTime endDateTime;
		
	/**
	 * Constructor for the class, it receives the start and end datetime
	 * 
	 * @param startDateTime  start datetime
	 * @param endDateTime    end datetime
	 */
	@JsonCreator
	public TimeInterval(
			@JsonProperty("start")LocalDateTime startDateTime,
			@JsonProperty("end")LocalDateTime endDateTime) 
	{
		super();
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
	}

	/**
	 * Gets the start datetime
	 * @return start datetime
	 */
	public LocalDateTime getStart() 
	{
		return startDateTime;
	}

	/**
	 * Gets the end datetime
	 * @return  Gets the end datetime
	 */
	public LocalDateTime getEnd() 
	{
		return endDateTime;
	}
	
	/**
	 * Serialize the time interval into JSON.
	 * 
	 * @return Json object 
	 */
	public String toJson(){
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return json;
	}
	
	/**
	 *  Serialize the time interval into String.
	 *  
	 *  @return String object
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("start: ").append(startDateTime.toString()).append(",");
		sb.append("end: ").append(endDateTime.toString());
		return sb.toString();
	}
}
