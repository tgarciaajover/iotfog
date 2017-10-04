package com.advicetec.measuredentitity;

import java.io.IOException;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.configuration.ConfigurationObject;
import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;
import com.advicetec.core.serialization.MeasuringStateSerializer;
import com.advicetec.core.serialization.MeasuringStateDeserializer;

/**
 * This class models transition between two Measured states.
 * 
 * @author advicetec
 * 
 * @see MeasuringState
 */
public class MeasuredEntityStateTransition extends ConfigurationObject 
{

	static final Logger logger = LogManager.getLogger(MeasuredEntityStateBehavior.class.getName());
	
	/**
	 * initial state 
	 */
	@JsonProperty("state_from")
	@JsonSerialize(using = MeasuringStateSerializer.class)
	@JsonDeserialize(using = MeasuringStateDeserializer.class)	
	private MeasuringState stateFrom;
	
	/**
	 * reason code of the initial state
	 */
	@JsonProperty("reason_code")
	private int reasonCode;

	/**
	 * creation date time 
	 */
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	private LocalDateTime createDate;

	
	/**
	 * behavior code to execute.
	 */
	@JsonProperty("behavior")
	private int behavior;  
	
	/**
	 * Measured entity state transition identifier
	 * 
	 * @param id	internal measured entity state transition.
	 */
	public MeasuredEntityStateTransition(@JsonProperty("id") Integer id) {
		super(id);
	}

	/**
	 * Gets the measure entity state from.
	 *  
	 * @return measure entity state from.
	 */
	public MeasuringState getStateFrom() {
		return stateFrom;
	}

	/**
	 * Sets the measure entity state from.
	 * 
	 * @param stateFrom measure entity state from.
	 */
	public void setStateFrom(MeasuringState stateFrom) {
		this.stateFrom = stateFrom;
	}

	/**
	 * Gets the reason code 
	 * 
	 * @return reason code
	 */
	public int getResonCode() {
		return reasonCode;
	}

	/**
	 * Sets the reason code 
	 * 
	 * @param resonCode
	 */
	public void setResonCode(int resonCode) {
		this.reasonCode = resonCode;
	}

	/**
	 * Gets the creation date
	 * 
	 * @return creation date
	 */
	public LocalDateTime getCreateDate() {
		return createDate;
	}

	/**
	 * Sets the creation date
	 * 
	 * @param createDate
	 */
	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}

	/**
	 * Gets the behavior text to execute.
	 * 
	 * @return	behavior text to execute.
	 */
	public int getBehavior() {
		return behavior;
	}

	/**
	 * Sets the behavior text to execute.
	 * 
	 * @param behavior	behavior text to execute.
	 */
	public void setBehavior(int behavior) {
		this.behavior = behavior;
	}

	/**
	 * Json representation
	 * 
	 * @return	Json string representation.
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
