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


/**
 * Class representing a measured entity schedule event. It defines the date and time when the event 
 * must be executed as well as how often it should be repeated.   
 * 
 * @author advice
 *
 */
public class MeasuredEntityScheduledEvent extends  ConfigurationObject
{
	
	static final Logger logger = LogManager.getLogger(MeasuredEntityScheduledEvent.class.getName());
	
	/**
	 * type of scheduled event  
	 */
	@JsonProperty("scheduled_event_type")
	String scheduledEventType;
	
	/**
	 * scheduled event description 
	 */
	@JsonProperty("descr")
	String descr;
	
	/**
	 * recurrence definition 
	 */
	@JsonProperty("recurrences")
	String recurrence;

	/**
	 * time of the day when the event must be executed. 
	 */
	@JsonProperty("day_time")
	@JsonSerialize(using = LocalTimeSerializer.class)
	@JsonDeserialize(using = LocalTimeDeserializer.class)	
	LocalTime dayTime;
	
	/**
	 * date and time when the scheduled event has been created. 
	 */
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	private LocalDateTime createDate;
	
	/**
	 * This list maintains references to events scheduled and created in the delay event queue. 
	 */
	@JsonIgnore
	List<String> referencedEvents;
		
	/**
	 * Constructor for the class 
	 * 
	 * @param id					internal identifier for the scheduled event.
	 * @param scheduledEventType	type of scheduled event to create.
	 */
	@JsonCreator
	public MeasuredEntityScheduledEvent(@JsonProperty("id") Integer id, @JsonProperty("scheduled_event_type") String scheduledEventType) {
		super(id);
		this.scheduledEventType = scheduledEventType;
		this.referencedEvents = new ArrayList<String>();
	}

	/**
	 * Sets the scheduled event type 
	 * 
	 * @param scheduledEventType	type to set.
	 */
	public void setScheduledEventType(String scheduledEventType) {
		this.scheduledEventType = scheduledEventType;
	}

	/**
	 * Sets the description to the scheduled event. 
	 *  
	 * @param descr	description to set.
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}

	/**
	 * Sets a recurrence for the scheduled event.
	 * 
	 * @param recurrence	recurrence to set.
	 */
	public void setRecurrence(String recurrence) {
		this.recurrence = recurrence;
	}
	
	/**
	 * Gets the time of the day when the event should be executed. 
	 * 
	 * @return   time of the day when the event should be executed.
	 */
	public LocalTime getDayTime() {
		return dayTime;
	}

	/**
	 * Sets the time of the day when the event should be executed.
	 * 
	 * @param dayTime	time of the day when the event should be executed.
	 */
	public void setDayTime(LocalTime dayTime) {
		this.dayTime = dayTime;
	}

	/**
	 * Gets the scheduled event type 
	 * @return	scheduled event type 
	 */
	public String getScheduledEventType() {
		return scheduledEventType;
	}

	/**
	 * Gets the schedule event description
	 * 
	 * @return	schedule event description
	 */
	public String getDescr() {
		return descr;
	}

	/**
	 * Gets the schedule event recurrence
	 * 	
	 * @return	schedule event recurrence
	 */
	public String getRecurrence() {
		return recurrence;
	}

	/**
	 * Gets the creation date
	 * 
	 * @return	creation date
	 */
	public LocalDateTime getCreateDate() {
		return createDate;
	}

	/**
	 * Sets the creation date
	 * 
	 * @param createDate date and time when the event was created.
	 */
	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}
	
	/**
	 * Adds and unique identifier of an event to the referenced event list.  
	 * 
	 * @param uuid	 unique identifier of the event.
	 */
	public void addReferencedEvent(String uuid){
		this.referencedEvents.add(uuid);
	}
	
	/**
	 * Gets the list of referenced events.
	 * 
	 * @return	referenced events list.
	 */
	public List<String> getReferencedEvents()
	{
		return this.referencedEvents;
	}
	
	/**
	 * string representation.
	 * 
	 */
	public String toString(){
		return "Id:" + Integer.toString(this.getId()) + 
					" type:" + this.getScheduledEventType() + 
						" descr:" + this.getDescr() + 
							"recurrence:" +this.getRecurrence(); 
	}
	
	/**
	 * Gets a json representation.
	 * 
	 * @return  scheduled event json representation 
	 */
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
