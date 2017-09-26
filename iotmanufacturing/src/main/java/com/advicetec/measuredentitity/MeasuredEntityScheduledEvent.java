package com.advicetec.measuredentitity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.configuration.ConfigurationObject;
import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;
import com.advicetec.core.serialization.LocalTimeSerializer;
import com.advicetec.core.serialization.LocalTimeDeserializer;


public class MeasuredEntityScheduledEvent extends  ConfigurationObject
{
	
	static final Logger logger = LogManager.getLogger(MeasuredEntityScheduledEvent.class.getName());
	
	@JsonProperty("scheduled_event_type")
	String scheduledEventType;
	
	@JsonProperty("descr")
	String descr;
	
	@JsonProperty("recurrences")
	String recurrence;

	@JsonProperty("day_time")
	@JsonSerialize(using = LocalTimeSerializer.class)
	@JsonDeserialize(using = LocalTimeDeserializer.class)	
	LocalTime dayTime;
	
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	private LocalDateTime createDate;
	
	/**
	 * This list maintains references to events scheduled and created in the delay queue. 
	 */
	@JsonIgnore
	List<String> referencedEvents;
		
	@JsonCreator
	public MeasuredEntityScheduledEvent(@JsonProperty("id") Integer id, @JsonProperty("scheduled_event_type") String scheduledEventType) {
		super(id);
		this.scheduledEventType = scheduledEventType;
		this.referencedEvents = new ArrayList<String>();
	}

	public void setScheduledEventType(String scheduledEventType) {
		this.scheduledEventType = scheduledEventType;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public void setRecurrence(String recurrence) {
		this.recurrence = recurrence;
	}
	
	public LocalTime getDayTime() {
		return dayTime;
	}

	public void setDayTime(LocalTime dayTime) {
		this.dayTime = dayTime;
	}

	public String getScheduledEventType() {
		return scheduledEventType;
	}

	public String getDescr() {
		return descr;
	}

	public String getRecurrence() {
		return recurrence;
	}

	public LocalDateTime getCreateDate() {
		return createDate;
	}

	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}
	
	public void addReferencedEvent(String uuid){
		this.referencedEvents.add(uuid);
	}
	
	public List<String> getReferencedEvents()
	{
		return this.referencedEvents;
	}
	
	public String toString(){
		return "Id:" + Integer.toString(this.getId()) + 
					" type:" + this.getScheduledEventType() + 
						" descr:" + this.getDescr() + 
							"recurrence:" +this.getRecurrence(); 
	}
	
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
