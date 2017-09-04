package com.advicetec.configuration;

import java.io.IOException;
import java.time.LocalDateTime;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.core.serialization.BooleanDeserializer;
import com.advicetec.core.serialization.BooleanSerializer;
import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;

public class ReasonCode extends ConfigurationObject 
{
	/**
	 * Canonical code for the reason code.
	 */
	@JsonProperty("reason_id")
	private String cannonicalReasonId;
	
	/**
	 * Canonical code for the company where this reason code is valid.
	 */
	@JsonProperty("company")
	private String cannonicalCompany;
	
	/**
	 * Canonical code for the location where this reason code is valid. 
	 */
	@JsonProperty("location")
	private String cannonicalLocation;
	
	/**
	 * Canonical code for the plant where this reason code is valid.
	 */
	@JsonProperty("plant")
	private String cannonicalPlant;
	
	/**
	 * Reason code's description.
	 */
	@JsonProperty("descr")
	String description;
	
	/**
	 * Reason Group 
	 */
	@JsonProperty("group_cd")
	String group;
	
	/**
	 * Classification for the OEE (performance, availability, )
	 */
	@JsonProperty("classification")
	String classification;

	/**
	 * It is yes when the stops decrease the available time.
	 */
	@JsonProperty("down")
	@JsonSerialize(using = BooleanSerializer.class)
	@JsonDeserialize(using = BooleanDeserializer.class)	
	Boolean idleDown;
	
	/**
	 * Identified root cause for the stop.
	 */
	@JsonProperty("cause")
	String rootCause; 
	
	/**
	 * date and time wen the reason code was registered in the system.
	 */
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
    private LocalDateTime createDate;

	/**
	 * Constructor for the object. 
	 * @param id  identifier of reason code in the configuration database.
	 * @param description  description of the reason code.
	 */
	public ReasonCode(
			@JsonProperty("id")Integer id, 
			@JsonProperty("descr")String description) {
		super(id);
		this.description = description;
	}
			
	/**
	 * Gets the description
	 * @return  description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the reason code group 
	 * @return reason code's group.
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Sets the group of the reason code
	 * @param group reason code group
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Gets the reason code's classification 
	 * @return  reason code's classification 
	 */
	public String getClassification() {
		return classification;
	}

	/**
	 * Sets the reason code classification 
	 * @param classification 
	 */
	public void setClassification(String classification) {
		this.classification = classification;
	}

	/**
	 * Gets the reason code's root cause
	 * @return  root cause
	 */
	public String getRootCause() {
		return rootCause;
	}

	/**
	 * Sets the reason code cause
	 * @param rootCause  root cause.
	 */
	public void setRootCause(String rootCause) {
		this.rootCause = rootCause;
	}

	/**
	 * Return whether the reason code decreases or not the avaiable time 
	 * 
	 * @return True, it decreases the available time, false otherwise.
	 */
	@JsonIgnore
	public boolean isIdleDown() {
		return idleDown;
	}

	/**
	 * Gets the idle down condition
	 * @return idle down condition
	 */
	public boolean getIdleDown() {
		return this.idleDown;
	}
	
	/**
	 * Return whether the reason code counts or not the productive time
	 * 
	 * @return  True, it counts in the productive time, false otherwise.
	 */
	@JsonIgnore
	public boolean includeProductiveTime()
	{
		return !(this.idleDown); 
	}
	
	/**
	 * Sets the idle down condition
	 * 
	 * @param idleDown idle down condition
	 */
	public void setIdleDown(boolean idleDown) {
		this.idleDown = idleDown;
	}

	/**
	 * Gets the date when the reason code was registered in the system.
	 * 
	 * @return registered datetime
	 */
	public LocalDateTime getCreateDate() {
		return createDate;
	}

	/**
	 * Sets the date when the reason code was registered in the system.
	 * 
	 * @param createDate  registered datatime
	 */
	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}

	/**
	 * Puts the reason code in string format.
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("id:").append(getId()).append(",");
		sb.append("description:").append(description);
		return sb.toString();
	}

	/**
	 * Creates a Json representation of the reason code
	 * @return JSON representation.
	 */
	public String toJson() {
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	/**
	 * Sets the canonical reason id 
	 * @param cannonicalReasonId canonical reason identifier 
	 * 
	 */
	public void setCannonicalReasonId(String cannonicalReasonId) {
		this.cannonicalReasonId = cannonicalReasonId;
	}

	/**
	 * Sets the canonical company code
	 * @param cannonicalCompany canonical company code
	 */
	public void setCannonicalCompany(String cannonicalCompany) {
		this.cannonicalCompany = cannonicalCompany;
	}

	/**
	 * Sets the canonical location code.
	 * @param cannonicalLocation  canonical location
	 */
	public void setCannonicalLocation(String cannonicalLocation) {
		this.cannonicalLocation = cannonicalLocation;
	}

	/**
	 * Sets the canonical plant code
	 * @param cannonicalPlant canonical plant code
	 */
	public void setCannonicalPlant(String cannonicalPlant) {
		this.cannonicalPlant = cannonicalPlant;
	}

	/**
	 * Gets the canonical reason code 
	 * @return  canonical reason code 
	 */
	public String getCannonicalReasonId() {
		return cannonicalReasonId;
	}

	/**
	 * Gets the canonical company code 
	 * 
	 * @return  canonical company code
	 */
	public String getCannonicalCompany() {
		return cannonicalCompany;
	}

	/**
	 * Gets the canonical location code
	 * 
	 * @return canonical location code
	 */
	public String getCannonicalLocation() {
		return cannonicalLocation;
	}

	/**
	 * Gets the canonical plant code
	 * 
	 * @return canonical plant code
	 */
	public String getCannonicalPlant() {
		return cannonicalPlant;
	}

}
