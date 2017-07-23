package com.advicetec.configuration;

import java.io.IOException;
import java.time.LocalDateTime;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;

public class ReasonCode extends ConfigurationObject 
{
	@JsonProperty("reason_id")
	private String cannonicalReasonId;
	@JsonProperty("company")
	private String cannonicalCompany;
	@JsonProperty("location")
	private String cannonicalLocation;
	@JsonProperty("plant")
	private String cannonicalPlant;
	
	@JsonProperty("descr")
	String description;
	/* tipo de falla */
	@JsonProperty("group")
	String group;
	/* clasificacion para el OEE*/
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

	@JsonIgnore
	public boolean isIdleDown() {
		return idleDown;
	}

	public boolean getIdleDown() {
		return this.idleDown;
	}
	
	@JsonIgnore
	public boolean includeProductiveTime()
	{
		return !(this.idleDown); 
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
	
	public void setCannonicalReasonId(String cannonicalReasonId) {
		this.cannonicalReasonId = cannonicalReasonId;
	}

	public void setCannonicalCompany(String cannonicalCompany) {
		this.cannonicalCompany = cannonicalCompany;
	}

	public void setCannonicalLocation(String cannonicalLocation) {
		this.cannonicalLocation = cannonicalLocation;
	}

	public void setCannonicalPlant(String cannonicalPlant) {
		this.cannonicalPlant = cannonicalPlant;
	}

	public String getCannonicalReasonId() {
		return cannonicalReasonId;
	}

	public String getCannonicalCompany() {
		return cannonicalCompany;
	}

	public String getCannonicalLocation() {
		return cannonicalLocation;
	}

	public String getCannonicalPlant() {
		return cannonicalPlant;
	}

}
