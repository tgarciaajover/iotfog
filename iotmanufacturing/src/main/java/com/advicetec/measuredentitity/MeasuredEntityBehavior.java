package com.advicetec.measuredentitity;

import java.io.IOException;
import java.time.LocalDateTime;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.configuration.ConfigurationObject;
import com.advicetec.configuration.LocalDateTimeDeserializer;
import com.advicetec.configuration.LocalDateTimeSerializer;

public class MeasuredEntityBehavior extends  ConfigurationObject
{
	
	@JsonProperty("id")
	Integer id; 
	
	@JsonProperty("name")
	String name;
	
	@JsonProperty("descr")
	String descr;
	
	@JsonProperty("behavior_text")
	String behaviorText;

	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	private LocalDateTime createDate;
		
	@JsonCreator
	public MeasuredEntityBehavior(@JsonProperty("id") Integer id, @JsonProperty("name") String name) {
		super(id);
		this.name = name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public void setBehaviorText(String behaviorText) {
		this.behaviorText = behaviorText;
	}

	public String getName() {
		return name;
	}

	public String getDescr() {
		return descr;
	}

	public String getBehavior_text() {
		return behaviorText;
	}

	public LocalDateTime getCreateDate() {
		return createDate;
	}

	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}
	
	
	public String toJson()
	{
		ObjectMapper mapper = new ObjectMapper();
			
		String jsonInString=null;
		try {
			
			jsonInString = mapper.writeValueAsString(this);
			
			
		} catch (JsonGenerationException e) {
			// TODO: log the error
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return jsonInString;
	}	
	
}
