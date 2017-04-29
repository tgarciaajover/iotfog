package com.advicetec.configuration;

import java.io.IOException;
import java.time.LocalDateTime;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;

public class ReasonCode extends ConfigurationObject 
{
	
	@JsonProperty("descr")
	String description;
	
	@JsonProperty("group")
	String group;
	
	@JsonProperty("classification")
	String classification;

	@JsonProperty("idle_down")
	Boolean idleDown;
	
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
    private LocalDateTime createDate;

	
	public ReasonCode(
			@JsonProperty("id")Integer id, 
			@JsonProperty("descr")String description) {
		super(id);
		this.description = description;
	}
			
	public String getDescription() {
		return description;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public boolean isIdleDown() {
		return idleDown;
	}

	public void setIdleDown(boolean idleDown) {
		this.idleDown = idleDown;
	}

	public LocalDateTime getCreateDate() {
		return createDate;
	}

	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("id:").append(getId()).append(",");
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
