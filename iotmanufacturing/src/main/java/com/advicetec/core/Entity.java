package com.advicetec.core;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.advicetec.configuration.ConfigurationObject;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.eventprocessor.AggregationEvent;
import com.advicetec.eventprocessor.AggregationEventType;
import com.advicetec.measuredentitity.MeasuredEntityScheduledEvent;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.MeasuringState;

public abstract class Entity extends ConfigurationObject {

	static final Logger logger = LogManager.getLogger(Entity.class.getName());

	/**
	 * maximum state interval measured in seconds before is saved.
	 */
	static Integer MAX_INTERVAL_TIME = 300; 

	/**
	 * Measured entity type  
	 */
	@JsonIgnore
	protected MeasuredEntityType type; 

    /**
     * Canonical key used to reference instance in host systems. 
     */
    protected String canonicalKey;
	
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
	 * maximum amount of seconds that an interval can be open.
	 */
	@JsonIgnore
	protected Integer maxTimeForInterval;

	/**
	 * List of scheduled events registered for this measured entity. 
	 * They are stores as a map to make it easy for reading and writing   
	 */
	@JsonIgnore
	protected Map<Integer, MeasuredEntityScheduledEvent> scheduledEvents;

	
	public Entity(Integer id, MeasuredEntityType type) 
	{
		super(id);
		
		this.type = type;
		this.maxTimeForInterval = MAX_INTERVAL_TIME; 

		this.attributes = new CopyOnWriteArrayList<Attribute>();
		this.attributeValues = new CopyOnWriteArrayList<AttributeValue>();
		this.scheduledEvents = new ConcurrentHashMap<Integer, MeasuredEntityScheduledEvent>();
		
	}

	/**
	 * Gets the measured entity type
	 * 
	 * @return	measured entity type
	 */
	public synchronized MeasuredEntityType getType()
	{
		return this.type;
	}

	/**
	 * Gets the maximum interval time 
	 * 
	 * @return	maximum interval time
	 */
	public synchronized Integer getMaxTimeForInterval() {
		return maxTimeForInterval;
	}

	public synchronized void setMaxTimeForInterval(Integer maxTimeForInterval) {
		this.maxTimeForInterval = maxTimeForInterval;
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
	 * Gets the list of attributes 
	 * 
	 * @return attribute list
	 */
	@JsonIgnore
	public synchronized List<Attribute> getAttributeList(){
		return attributes;
	}

	/**
	 * Register a new attribute in the measured entity
	 * 
	 * @param attrMeasureEntity  attribute to register
	 * @return true if successful, false otherwise.
	 */
	@JsonIgnore
	public synchronized boolean registerAttribute(Attribute attrMeasureEntity){
		return attributes.add(attrMeasureEntity);
	}
	
	/**
	 * Gets an scheduled event by its unique internal identifier
	 * 
	 * @param id	scheduled event unique internal identifier
	 * 
	 * @return	scheduled event instance is found, null otherwise
	 */
	public synchronized MeasuredEntityScheduledEvent getScheduledEvent (Integer id){
		return this.scheduledEvents.get(id);
	}

	/**
	 * Gets the list of aggregation events to put in the event list
	 * 
	 * @return List of aggregation events to schedule.
	 */
	public synchronized List<AggregationEvent> getScheduledEvents(){

		List<AggregationEvent> ret = new ArrayList<AggregationEvent>();

		for (Integer key: this.scheduledEvents.keySet()) {
			MeasuredEntityScheduledEvent scheduledEvent = this.scheduledEvents.get(key);
			// According to the type of event, we create the instance class.

			if (scheduledEvent.getScheduledEventType().equals("AG")) {

				String lines[] = scheduledEvent.getRecurrence().split("\\r?\\n");

				for (String recurrence : lines) {
					AggregationEvent aggEvent = new AggregationEvent(getId(), getType(), AggregationEventType.OEE, recurrence, scheduledEvent.getDayTime());
					ret.add(aggEvent);
				}
			} else {
				logger.error("The Schedule event given is not being handled - Type given:" +  scheduledEvent.getScheduledEventType() );
			}
		}

		return ret;

	}

	/**
	 * Gets the aggregation event by its unique internal identifier
	 * 
	 * @param id	aggregation event unique internal identifier
	 * 
	 * @return		a list of aggregation events.
	 */
	public synchronized List<AggregationEvent> getScheduledEvents(Integer id){

		List<AggregationEvent> ret = new ArrayList<AggregationEvent>();

		MeasuredEntityScheduledEvent scheduledEvent  = getScheduledEvent(id);

		if (scheduledEvent != null) {
			// According to the type of event, we create the instance class.

			if (scheduledEvent.getScheduledEventType().equals("AG")) {

				String lines[] = scheduledEvent.getRecurrence().split("\\r?\\n");

				for (String recurrence : lines) {
					AggregationEvent aggEvent = new AggregationEvent(getId(), getType(), AggregationEventType.OEE, recurrence, scheduledEvent.getDayTime());
					ret.add(aggEvent);
				}
			} else {
				logger.error("The Schedule event given is not being handled - Type given:" +  scheduledEvent.getScheduledEventType() );
			}
		}

		return ret;
	}


	/**
	 * Gets an attribute value by name 
	 * 
	 * @param name name of the attribute which value should be returned
	 * 
	 * @return  Attribute name or null if not found.
	 */
	@JsonIgnore
	public synchronized AttributeValue getAttributeValue(String name){

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
	 * Gets the attribute list registered in the measured entity
	 * 
	 * @return List of attributes registered.
	 */
	@JsonIgnore
	public synchronized List<AttributeValue> getAttributeValueList(){
		return attributeValues;
	}

	/**
	 * Registers a new attribute value in the measured entity
	 * 
	 * @param value  attribute value to register
	 * 
	 * @return true if the insert was successful, false otherwise
	 */
	@JsonIgnore
	public synchronized boolean registerAttributeValue(AttributeValue value){
		return attributeValues.add(value);
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
	
}
