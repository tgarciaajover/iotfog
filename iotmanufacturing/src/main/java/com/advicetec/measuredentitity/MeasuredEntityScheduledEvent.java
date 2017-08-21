package com.advicetec.measuredentitity;

import java.io.IOException;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.configuration.ConfigurationObject;
import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;

public class MeasuredEntityScheduledEvent extends  ConfigurationObject
{
	
	static final Logger logger = LogManager.getLogger(MeasuredEntityScheduledEvent.class.getName());
	
	@JsonProperty("scheduled_event_type")
	String scheduledEventType;
	
	@JsonProperty("descr")
	String descr;
	
	@JsonProperty("recurrences")
	String recurrence;

	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	private LocalDateTime createDate;
		
	@JsonCreator
	public MeasuredEntityScheduledEvent(@JsonProperty("id") Integer id, @JsonProperty("scheduled_event_type") String scheduledEventType) {
		super(id);
		this.scheduledEventType = scheduledEventType;
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
