package com.advicetec.measuredentitity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
public class ExecutedEntity extends ConfigurationObject 
{

	static final Logger logger = LogManager.getLogger(ExecutedEntity.class.getName()); 

	/**
	 * maximum state interval measured in seconds before is saved.
	 */
	static Integer MAX_INTERVAL_TIME = 300; 

	
	/**
	 * Date and time when the instance was created.  
	 */
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	private LocalDateTime createDate;

	/**
	 * Last date and time when the instance was updated  
	 */
	@JsonProperty("last_updttm") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	private LocalDateTime lastDateTime;

	/**
	 * Type of object 
	 */
	@JsonIgnore
	MeasuredEntityType type;
	
    /**
     * start date-time of the current interval.
     */
    @JsonIgnore
    protected LocalDateTime startDateTimeStatus; 

    /**
     * state of the current interval.
     */
    @JsonIgnore
    private MeasuringState currentState; 

    /**
     * the reason code for the current state.
     */
    private ReasonCode currentReason;  

    /**
     * List of attributes registered in this executed entity 
     */
    @JsonIgnore
    protected List<Attribute> attributes;

    /**
     * List of attribute values registered in this executed entity
     */
    @JsonIgnore
    protected List<AttributeValue> attributeValues;

    /**
     * Canonical key used to reference instance in host systems. 
     */
    protected String canonicalKey;
	
	/**
	 * maximum amount of seconds that an interval can be open.
	 */
	@JsonIgnore
	protected Integer maxTimeForInterval;
    

	/**
	 * Constructor for the class 
	 * @param id	executed entity identification
	 * @param type	type of executed entity
	 */
	public ExecutedEntity(@JsonProperty("id") Integer id, MeasuredEntityType type) {
		super(id);
		startDateTimeStatus = LocalDateTime.now();
		currentState = MeasuringState.SCHEDULEDOWN;
		currentReason = null;
		maxTimeForInterval = MAX_INTERVAL_TIME; 
		
		attributes = new ArrayList<Attribute>();
		attributeValues = new ArrayList<AttributeValue>();
		this.type = type;
	}

    /**
     * Gets an attribute by name
     * 
     * @param name attribute name to return
     * @return attribute with the name given as parameter, or NULL if not found.
     */
    @JsonIgnore
    public Attribute getAttribute(String name){
    	  
		for (int i = 0; i < this.attributes.size(); i++){
			Attribute attr = this.attributes.get(i);
			if ((attr.getName()).compareTo(name) == 0){
				return attr;
			}
		}
		
		return null;
    }
    
    /**
     * Returns the value of an attribute by the given name.
     * 
     * @param name	attribute name to return.
     * @return	Attribute value with the name given as parameter, or NULL if not found.
     */
    public AttributeValue getAttributeValue(String name){
    	
    	logger.debug("Starting getAttributeValue - attribute:" + name);  
    	
		for (int i = 0; i < this.attributeValues.size(); i++){
			AttributeValue attr = this.attributeValues.get(i);
			if ((attr.getAttr().getName()).compareTo(name) == 0){
				return attr;
			}
		}
    	
		logger.debug("ending getAttributeValue with null");
		
		return null;
    }
    
    /**
     * Returns the list of attributes registered to this entity.
     * 
     * @return attribute list registered to this entity.
     */
    @JsonIgnore
    public List<Attribute> getAttributeList(){
    	return attributes;
    }
    
    /**
     * Registers a new attribute in this entity.
     * 
     * @param attrribute a new attribute to register.
     * @return	TRUE if the attribute was successfully registered, FALSE otherwise.
     */
    public boolean registerAttribute(Attribute attrribute){
    	return attributes.add(attrribute);
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
     * Returns the list of attribute values registered to this entity.
     * 
     * @return attribute value list.
     */
    @JsonIgnore
    public List<AttributeValue> getAttributeValueList(){
    	return attributeValues;
    }

    /**
     * Registers an attribute value to this entity.
     * 
     * @param value updated attribute value to register.
     * @return TRUE if the attribute value was successfully registered, FALSE otherwise.
     */
    @JsonIgnore
    public boolean registerAttributeValue(AttributeValue value){
    	return attributeValues.add(value);
    }
    
    /**
     * Returns the type of this entity.
     * 
     * @return type of executed entity.
     * @see MeasuredEntityType
     */
    @JsonIgnore
    public MeasuredEntityType getType(){
    	return type;
    }
        
    /**
     * Registers the start of a new state interval in the entity.
     * 
     * @param newState The state of the new interval.
     * @param rCode	the reason code for the new interval.
     */
    @JsonIgnore
    public void startInterval(LocalDateTime dateTime, MeasuringState newState, ReasonCode rCode) {
    	currentState = newState;
    	currentReason= rCode;
    	startDateTimeStatus = dateTime;
    }
    
    /**
     * Gets the entity's current state 
     * 
     * @return the entity's current state.
     */
    @JsonIgnore
    public MeasuringState getCurrentState(){
    	return this.currentState;
    }
    
    /**
     * Gets the entity's current state reason
     * 
     * @return	current reason code.
     */
    @JsonIgnore
    public ReasonCode getCurrentReason(){
    	return this.currentReason;
    }
    
    /**
     * Gets the date and time when the current state interval started.
     *  
     * @return the start date and time for the current state interval.
     */
    @JsonIgnore
    public LocalDateTime getCurrentStatDateTime(){
    	return this.startDateTimeStatus;
    }

	/**
	 * Sets the canonical key for this entity in the host system.
	 * 
	 * @param canonicalKey canonical key in the host system.
	 */
	public void setCanonicalKey(String canonicalKey) {
		this.canonicalKey = canonicalKey;
	}

    /**
     * Gets the canonical key for this entity in the host system
     * 
     * @return	canonical key in the host system.
     */
    public String getCanonicalKey(){
    	return this.canonicalKey;
    }

	/**
	 * Gets the maximum interval time 
	 * 
	 * @return	maximum interval time
	 */
	public synchronized Integer getMaxTimeForInterval() {
		return maxTimeForInterval;
	}

    
	/**
	 * This function verifies if the current interval state should be calculated and saved. 
	 * 
	 * @return TRUE if we should start a new interval, FALSE otherwise.
	 */
	public synchronized boolean startNewInterval() {

		if (getCurrentStatDateTime().plusSeconds(getMaxTimeForInterval()).isBefore(LocalDateTime.now()))
			return true;

		return false;
	}

}
