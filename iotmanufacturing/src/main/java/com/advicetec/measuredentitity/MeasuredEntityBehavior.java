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
 * This class represents a language behavior associated to a Measured Entity.
 * @author advicetec
 *
 */
public class MeasuredEntityBehavior extends  ConfigurationObject
{
	static final Logger logger = LogManager.getLogger(MeasuredEntityBehavior.class.getName());
	
	/**
	 * identifier
	 */
	@JsonProperty("id")
	Integer id; 
	
	/**
	 * behavior name.
	 */
	@JsonProperty("name")
	String name;
	
	/**
	 * Behavior description
	 */
	@JsonProperty("descr")
	String descr;
	
	/**
	 * Text that represents the behavior in the language.
	 */
	@JsonProperty("behavior_text")
	String behaviorText;

	/** 
	 * Timestamp when the behavior is created.
	 */
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	private LocalDateTime createDate;
	
	/**
	 * Constructor.
	 * @param id identifier.
	 * @param name behavior name.
	 */
	@JsonCreator
	public MeasuredEntityBehavior(@JsonProperty("id") Integer id, @JsonProperty("name") String name) {
		super(id);
		this.name = name;
	}

	/**
	 * Sets the name of this behavior.
	 * @param name behavior's name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets a description for this behavior.
	 * @param descr behavior's description.
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}

	/**
	 * Sets the language text that codes this behavior. 
	 * @param behaviorText text with the behavior.
	 */
	public void setBehaviorText(String behaviorText) {
		this.behaviorText = behaviorText;
	}

	/**
	 * Returns the behavior's name.
	 * @return the behavior's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the behavior's description.
	 * @return the behavior's description.
	 */
	public String getDescr() {
		return descr;
	}

	/**
	 * Returns the text that codes the behavior.
	 * @return the text that codes the behavior.
	 */
	public String getBehavior_text() {
		return behaviorText;
	}

	/**
	 * Returns the timestamp when the behavior is created.
	 * @return the timestamp when the behavior is created.
	 */
	public LocalDateTime getCreateDate() {
		return createDate;
	}

	/**
	 * Sets the timestamp when the behavior is created.
	 * @param createDate timestamp when the behavior is created.
	 */
	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}
	
	/**
	 * Serialize the behavior to the JSON string.
	 * 
	 * @return the JSON string representation of this object.
	 */
	public String toJson()
	{
		ObjectMapper mapper = new ObjectMapper();
			
		String jsonInString=null;
		try {
			jsonInString = mapper.writeValueAsString(this);
		} catch (JsonGenerationException e) {
			logger.error("Cannot serialize this behavior into JSON");
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return jsonInString;
	}	
	
}
