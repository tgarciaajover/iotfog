package com.advicetec.measuredentitity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.applicationAdapter.ProductionOrder;
import com.advicetec.configuration.ConfigurationObject;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.Entity;
import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;

/**
 * This class represents any object being executed (produced) in a measured entity. 
 * 
 * Examples of classes inheriting from this class are:
 * 		ProductionOrder.
 * 
 * @author Advicetec
 * @see P
 */
@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type")
	@JsonSubTypes({
	    @Type(value = ProductionOrder.class, name = "J") })
public abstract class ExecutedEntity extends Entity 
{

	static final Logger logger = LogManager.getLogger(ExecutedEntity.class.getName()); 

	
	/**
	 * Date and time when the instance was created.  
	 */
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	protected LocalDateTime createDate;

	/**
	 * Last date and time when the instance was updated  
	 */
	@JsonProperty("last_updttm") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	protected LocalDateTime lastDateTime;
		

	/**
	 * This map contains the state of the executed object in every measured entity being produced.
	 * The Integer corresponds to the identifier of the measured entity.
	 */
	protected Map<Integer, EntityState > states;
	
	/**
	 * Constructor for the class 
	 * @param id	executed entity identification
	 * @param type	type of executed entity
	 */
	public ExecutedEntity(@JsonProperty("id") Integer id, MeasuredEntityType type) {
		super(id, type);
		
		states = new ConcurrentHashMap<Integer, EntityState>();
	}

           
    /**
     * Indicates if this entity is equals to some another executed entity. 
     * @param other	The other executed entity to compare against  
     * @return	TRUE if both, the given and this entities are equal, FALSE otherwise. 
     */
    @JsonIgnore
    public boolean equals(Object o){
    	
    	if (o instanceof ExecutedEntity) { 
	    	ExecutedEntity other = (ExecutedEntity) o;
	    	
	    	// we said that a production order is equal to another if both have the same 
	    	// attributes and their attribute values are also equal. 
	    	if (getId() != other.getId())
	    		return false;
	    	
	    	// Check if both orders have the same attributes.  
			for (int i = 0; i < this.attributes.size(); i++){
				Attribute attr = other.getAttribute(this.attributes.get(i).getName());
				if ((attr == null) || (attr.equals(this.attributes.get(i)) == false)){
					return false;
				}
			}
	
	    	// Check if both orders have the same attributes values.  
			for (int i = 0; i < this.attributeValues.size(); i++){
				AttributeValue attr = other.getAttributeValue(this.attributes.get(i).getName());
				if ((attr == null) || (attr.equals(this.attributeValues.get(i)) == false)){
					return false;
				}
			}
	    	return true;
    	} else {
    		return false;
    	}
    }

	/**
	 * Registers the start of a new interval in the measured entity
	 *   
	 * @param dateTime 	date and time when this interval should start
	 * @param newState	state given to the machine 
	 * @param rCode		Reason code for the interval
	 */
	@JsonIgnore
	public synchronized void startInterval(Integer measuredEntityId, LocalDateTime dateTime,  MeasuringState newState, ReasonCode rCode) {
		EntityState state = new EntityState(newState, rCode, dateTime); 
		this.states.put(measuredEntityId, state);
	}

	/**
	 * Establishes the reason code for the current interval
	 * 
	 * @param rCode  reason code to set.
	 */
	public synchronized boolean setCurrentReasonCode(Integer measuredEntityId, ReasonCode rCode){
		EntityState state = this.states.get(measuredEntityId);
		if (state != null) {
			state.setReason(rCode);
			this.states.put(measuredEntityId, state);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gets the state for the current interval.
	 * 
	 * @return current interval state.
	 */
	@JsonIgnore
	public synchronized MeasuringState getCurrentState(Integer measuredEntityId){
		
		EntityState state = this.states.get(measuredEntityId);
		if (state != null) {			
			return state.getState();
		} else {
			return MeasuringState.UNDEFINED;
		}
		
	}

	/**
	 * Gets the reason code for the current interval. 
	 * 
	 * @return	 current interval reason
	 */
	@JsonIgnore
	public synchronized ReasonCode getCurrentReason(Integer measuredEntityId)
	{
		EntityState state = this.states.get(measuredEntityId);
		if (state != null) {			
			return state.getReason();
		} else {
			return null;
		}		
	}

	/**
	 * Gets the date and time when the current interval started 
	 * 
	 * @return The date and time when the current interval started
	 */
	@JsonIgnore
	public synchronized LocalDateTime getCurrentStatDateTime(Integer measuredEntityId)
	{
		EntityState state = this.states.get(measuredEntityId);

		if (state != null) {			
			return state.getStartDateTimeStatus();
		} else {
			return null;
		}
		
	}

	/**
	 * This function verifies if the current interval state should be calculated and saved. 
	 * 
	 * @return TRUE if we should start a new interval, FALSE otherwise.
	 */
	public synchronized boolean startNewInterval(Integer measuredEntityId) {

		EntityState state = this.states.get(measuredEntityId);
		
		if (state.getStartDateTimeStatus().plusSeconds(getMaxTimeForInterval()).isBefore(LocalDateTime.now())) {
			return true;
		} else { 
			return false;
		}

	}
                   
}
