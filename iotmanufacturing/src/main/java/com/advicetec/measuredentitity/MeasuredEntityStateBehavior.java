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

/**
 * This class models a measured entity state behavior that will be executed when the measured entity 
 * enters in a particular state. 
 * 
 * @author advicetec
 *
 */
public class MeasuredEntityStateBehavior extends  ConfigurationObject
{
	
	static final Logger logger = LogManager.getLogger(MeasuredEntityStateBehavior.class.getName());
	
	/**
	 *  type of state behavior  
	 */
	@JsonProperty("state_behavior_type")
	String stateBehaviorType;
	
	/**
	 *  description of the state behavior 
	 */
	@JsonProperty("descr")
	String descr;
	
	/**
	 *  code to be executed within the change of the state  
	 */
	@JsonProperty("behavior_text")
	String behaviorText;

	/**
	 *  state behavior creation date  
	 */
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	private LocalDateTime createDate;
		
	/**
	 * Constructor for the class.
	 * 
	 * @param id					Internal identifier of the state behavior
	 * @param stateBehaviorType		State behavior type.
	 */
	@JsonCreator
	public MeasuredEntityStateBehavior(@JsonProperty("id") Integer id, @JsonProperty("state_behavior_type") String stateBehaviorType) {
		super(id);
		this.stateBehaviorType = stateBehaviorType;
	}

	/**
	 * Sets the state behavior type.
	 * 
	 * @param stateBehaviorType		State behavior type to be set.
	 */
	public void setStateBehaviorType(String stateBehaviorType) {
		this.stateBehaviorType = stateBehaviorType;
	}

	/**
	 * Sets the description of the state behavior
	 * 
	 * @param descr		description to be set.
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}

	/**
	 * Sets the text representing the code to be executed.
	 * 
	 * @param behaviorText	behavior text to execute.
	 */
	public void setBehaviorText(String behaviorText) {
		this.behaviorText = behaviorText;
	}

	/**
	 * Gets the state behavior type 
	 * 
	 * @return	state behavior type.
	 */
	public String getStateBehaviorType() {
		return stateBehaviorType;
	}

	/**
	 * Gets the description of the state behavior
	 * 
	 * @return	description of the state behavior
	 */
	public String getDescr() {
		return descr;
	}

	/**
	 * Gets the code to execute.
	 * 
	 * @return	code to execute
	 */
	public String getBehavior_text() {
		return behaviorText;
	}

	/**
	 * Gets the creation date 
	 * 
	 * @return	creation date.
	 */
	public LocalDateTime getCreateDate() {
		return createDate;
	}

	/**
	 * Sets the creation date
	 * 
	 * @param createDate  creation date 
	 */
	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return "Id:" + Integer.toString(this.getId()) + 
					" type:" + this.getStateBehaviorType() + 
						" descr:" + this.getDescr() + "text:" +this.getBehavior_text(); 
	}
	
	/**
	 * Json representation of this scheduled event. 
	 * 
	 * @return Json representation for this scheduled event.
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return jsonInString;
	}	
	
}
