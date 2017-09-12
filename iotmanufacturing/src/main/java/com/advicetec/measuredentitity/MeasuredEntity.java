package com.advicetec.measuredentitity;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.applicationAdapter.ProductionOrderFacade;
import com.advicetec.applicationAdapter.ProductionOrderManager;
import com.advicetec.configuration.ConfigurationObject;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.TimeInterval;
import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;
import com.advicetec.eventprocessor.AggregationEvent;
import com.advicetec.eventprocessor.AggregationEventType;

/**
 * Represents an entity being monitored by the measuring process.
 * 
 * It stores values being tracked in caches.
 * It stores states being registered in caches.
 * 
 * @author Advicetec
 *
 */
@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type")
	@JsonSubTypes({
	    @Type(value = Machine.class, name = "M"),
	    @Type(value = Plant.class, name = "P") })
public abstract class MeasuredEntity extends ConfigurationObject 
{
	
	static final Logger logger = LogManager.getLogger(MeasuredEntity.class.getName());
	
	/**
	 * maximum state interval measured in seconds
	 */
	static Integer MAX_INTERVAL_TIME = 300; 
	
	/**
	 * Code assigned to the measured entity to make easier the user interface. 
	 */
	@JsonProperty("code")
	protected String code;
	
	/**
	 * Measured entity type  
	 */
	@JsonIgnore
	protected MeasuredEntityType type; 

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
	private LocalDateTime lastUpdttm;
	
	/**
	 * Description given to the measured entity 
	 */
	@JsonProperty("descr")
	protected String descr;

	/**
	 * Set of behavior registered to the measured entity 
	 */
	@JsonIgnore
	protected List<MeasuredEntityBehavior> behaviors;
    
	/**
	 * Set of state behavior registered to this measured entity 
	 * 
	 * State behaviors are programs executed when the measured entity changes its state 
	 */
	@JsonIgnore
	protected List<MeasuredEntityStateBehavior> stateBehaviors;
	
	/**
	 * State transitions for the state machine that is executed to control the measured entity state. 
	 */
	@JsonIgnore
	protected List<MeasuredEntityStateTransition> stateTransitions;
	
    /**
     * start date-time of the current interval.
     */
    @JsonIgnore
    protected LocalDateTime startDateTimeStatus;
    
    /**
     * state of the current interval.
     */
    @JsonIgnore
    protected MeasuringState currentState;

    /**
     * the reason code for the current state.
     */
    @JsonIgnore
    protected ReasonCode currentReason;

    /**
     * List of executed entities being processed in this measured entity
     */
    @JsonIgnore
    protected Map<Integer, ExecutedEntity> executedEntities;
    
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
    
    /**
     * Constructor for the class 
     * @param id	internal unique identifier
     * @param type	type of measured entity
     */
    public MeasuredEntity( Integer id, MeasuredEntityType type) 
    {
		super(id);
		this.type = type;
		createDate = LocalDateTime.now();
		behaviors = new ArrayList<MeasuredEntityBehavior>();
		startDateTimeStatus = LocalDateTime.now();
		currentState = MeasuringState.SCHEDULEDOWN;
		currentReason = null;
		maxTimeForInterval = MAX_INTERVAL_TIME; 

		attributes = new ArrayList<Attribute>();
		attributeValues = new ArrayList<AttributeValue>();
		stateBehaviors = new ArrayList<MeasuredEntityStateBehavior>();
		stateTransitions = new ArrayList<MeasuredEntityStateTransition>();
		executedEntities = new HashMap<Integer, ExecutedEntity>();
		scheduledEvents = new HashMap<Integer, MeasuredEntityScheduledEvent>();
		
	}

    /**
     * Gets the code defined for the measured entity
     *  
     * @return  measured entity code
     */
    public synchronized String getCode() {
		return code;
	}

	/**
	 * Sets the measured entity's code
	 * @param code	measured entity's code
	 */
	public synchronized void setCode(String code) {
		this.code = code;
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
	 * Creates and returns a MessageAttributeValue
	 *  
	 * @param attribute  Attribute for which is created a new value  
	 * @param value 	 The value of this new measured attribute value
	 * @param timeStamp  Date and time when the new measured attribute value is created.
	 * 
	 * @return the new measured attribute value created
	 */
    public synchronized MeasuredAttributeValue getMeasureAttributeValue(Attribute attribute, Object value, LocalDateTime timeStamp)
    {
    	return new MeasuredAttributeValue(attribute, value, getId(), getType(), timeStamp);
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
     * 
     * @return true if successful, false otherwise.
     */
    @JsonIgnore
    public synchronized boolean registerAttribute(Attribute attrMeasureEntity){
    	return attributes.add(attrMeasureEntity);
    }
    
    /**
     * Performs the equals operator with another measured entity 
     * 
     * We implement equals comparing only their corresponding ids.
     * 
     * @param other	the measured entity to compare
     *  
     * @return true if they are equal, false otherwise
     */
    public synchronized boolean equals(MeasuredEntity other){
    	return getId().equals( other.getId());
    }
	
	/**
	 * Gets the creation date-time
	 *  
	 * @return date-time when the measured entity was created
	 */
	public synchronized LocalDateTime getCreateDate() {
		return createDate;
	}
	
	/**
	 * Sets the creation date-time
	 * 
	 * @param create_date	date-time when the measured entity was created
	 */
	public synchronized void setCreateDate(LocalDateTime create_date) {
		this.createDate = create_date;
	}
	
	/**
	 * Gets the last update date-time 
	 *  
	 * @return last date-time when the measured entity was updated
	 */
	public synchronized LocalDateTime getLastUpdttm() {
		return lastUpdttm;
	}

	/**
	 * Sets the last update date-time 
	 *  
	 * @param lastUpdttm	last date-time when the measured entity was updated
	 */
	public synchronized void setLastUpdttm(LocalDateTime lastUpdttm) {
		this.lastUpdttm = lastUpdttm;
	}

	/**
	 * Gets the description of the measured entity
	 * 
	 * @return 	measured entity description
	 */
	public synchronized String getDescr() {
		return descr;
	}
	
	/**
	 * Sets the measured entity description
	 * 
	 * @param descr	measured entity description
	 */
	public synchronized void setDescr(String descr) {
		this.descr = descr;
	}	
	
	/**
	 * Serialize the measure entity to JSON.
	 * 
	 * @return a JSON representation
	 */
	public synchronized String toJson()
	{
		ObjectMapper mapper = new ObjectMapper();
			
		String jsonInString=null;
		try {
			
			jsonInString = mapper.writeValueAsString(this);
			
			
		} catch (JsonGenerationException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return jsonInString;
	}

	/**
	 * Adds a behavior to the measured entity
	 * 
	 * @param id				internal behavior identifier  
	 * @param name				name of the behavior
	 * @param descr				behavior description
	 * @param behavior_text		behavior text
	 */
	public synchronized void putBehavior(Integer id,String name, String descr, String behavior_text)
	{
		boolean inserted = false; 
		for (int i = 0; i < this.behaviors.size(); i++){
			MeasuredEntityBehavior measuredEntityBehavior = this.behaviors.get(i);
			if (measuredEntityBehavior.getName().compareTo(name) == 0){
				MeasuredEntityBehavior measuredEntityBehavior2 = new MeasuredEntityBehavior(id, name);
				measuredEntityBehavior2.setDescr(descr);
				measuredEntityBehavior2.setBehaviorText(behavior_text);
				this.behaviors.remove(i);
				this.behaviors.add(measuredEntityBehavior2);
				inserted = true;
				break;
			}
		}
		
		if (inserted == false){
			MeasuredEntityBehavior measuredEntityBehavior2 = new MeasuredEntityBehavior(id, name);
			measuredEntityBehavior2.setDescr(descr);
			measuredEntityBehavior2.setBehaviorText(behavior_text);
			this.behaviors.add(measuredEntityBehavior2);
		}
	}
	
	/**
	 * Deletes a behavior from the measured entity
	 *  
	 * @param id 	internal behavior identifier
	 */
	public synchronized void removeBehavior(Integer id)
	{
		for (int i = 0; i < this.behaviors.size(); i++){
			MeasuredEntityBehavior measuredEntityBehavior = this.behaviors.get(i);
			if (measuredEntityBehavior.getId().compareTo(id) == 0){
				this.behaviors.remove(i);
				break;
			}
		}		
	}

	/**
	 * Deletes a scheduled event from the measured entity 
	 * 
	 * @param id	internal identifier given to the scheduled event.
	 */
	public synchronized void removeScheduledEvent(Integer id)
	{
		this.scheduledEvents.remove(id);
		
		// TODO: remove the event from the queue. 
	}
	
	/**
	 * Adds a new state behavior tom the measured entity
	 * 
	 * @param measuredEntityStateBehavior	Measured state behavior entity to add.
	 */
	public synchronized void putStateBehavior(MeasuredEntityStateBehavior measuredEntityStateBehavior)
	{
		logger.debug("Put State Behavior" + Integer.toString(this.stateBehaviors.size()));
		

		for (int i = 0; i < this.stateBehaviors.size(); i++){
			MeasuredEntityStateBehavior measuredEntityStateBehavior2 = this.stateBehaviors.get(i);
			if (measuredEntityStateBehavior2.getId().equals(measuredEntityStateBehavior.getId())){
				logger.debug("removed element");
				this.stateBehaviors.remove(i);
				break;
			}
		}
		
		this.stateBehaviors.add(measuredEntityStateBehavior);

		for (int i = 0; i < this.stateBehaviors.size(); i++){
			logger.debug("statebehavior:" + this.stateBehaviors.get(i).toString());
		}
		
		logger.debug("Method end. Num State Behavior" + Integer.toString(this.stateBehaviors.size()));
	}
	
	/**
	 * Adds a new Schedule event to the measured entity  
	 * 	
	 * @param measuredEntityScheduledEvent	schedule event to add 
	 */
	public synchronized void putScheduledEvent(MeasuredEntityScheduledEvent measuredEntityScheduledEvent)
	{
		logger.debug("Put Scheduled Event" + Integer.toString(this.scheduledEvents.size() ));
				
		this.scheduledEvents.put(measuredEntityScheduledEvent.getId(), measuredEntityScheduledEvent);
		
		// TODO: CREATE THE SCHEDULED EVENT IN THE QUEUE.
		
	}
	
	/**
	 * Adds a new state transition to the measured entity  
	 * 
	 * @param id				internal unique identifier for the state transition
	 * @param stateFrom			State from 
	 * @param reasonCodeFrom	state to
	 * @param behavior			behavior to execute
	 * @param createDate		create date-time
	 */
	public synchronized void putStateTransition(Integer id, MeasuringState stateFrom, Integer reasonCodeFrom, Integer behavior, LocalDateTime createDate)
	{
		logger.debug("Put State Transition");
		
		MeasuredEntityStateTransition measuredEntityStateTransitionNew = new MeasuredEntityStateTransition(id);
		measuredEntityStateTransitionNew.setStateFrom(stateFrom);
		measuredEntityStateTransitionNew.setResonCode(reasonCodeFrom);
		measuredEntityStateTransitionNew.setBehavior(behavior);
		measuredEntityStateTransitionNew.setCreateDate(createDate);

		for (int i = 0; i < this.stateTransitions.size(); i++){
			MeasuredEntityStateTransition measuredEntityStateTransition = this.stateTransitions.get(i);
			if (measuredEntityStateTransition.getId().equals(id) ){
				this.stateTransitions.remove(i);
				break;
			}
		}
		
		this.stateTransitions.add(measuredEntityStateTransitionNew);
		
	}
	
	/**
	 * Gets a behavior by name
	 * @param name	name of the behavior to return
	 * 
	 * @return	The behavior text or null if not found	
	 */
	public synchronized String getBehaviorText(String name)
	{
		logger.debug("MeasuredEntity:" + getId() + "behavior:" + name);
		
		for (int i = 0; i < this.behaviors.size(); i++){
			MeasuredEntityBehavior measuredEntityBehavior = this.behaviors.get(i);
			if (measuredEntityBehavior.getName().compareTo(name) == 0){
				return measuredEntityBehavior.getBehavior_text();
			}
		}
		return null;
	}

	/**
	 * Gets the state behavior text by type 
	 * @param stateBehaviorType	 state behavior type required.
	 * 
	 * @return	The state behavior text or null if not found
	 */
	public synchronized String getStateBehaviorText(String stateBehaviorType)
	{
		logger.debug("State Behavior:" + stateBehaviorType);
		
		for (int i = 0; i < this.stateBehaviors.size(); i++){
			MeasuredEntityStateBehavior measuredEntityStateBehavior = this.stateBehaviors.get(i);
			if (measuredEntityStateBehavior.getStateBehaviorType().compareTo(stateBehaviorType) == 0){
				return measuredEntityStateBehavior.getBehavior_text();
			}
		}
		return null;
	}
	
	/**
	 * Gets a state behavior registered by its unique internal identifier 
	 * 
	 * @param id	state behavior unique internal identifier
	 * 
	 * @return		MeasuredEntityStateBehavior instance of found , or null otherwise
	 */
	public synchronized MeasuredEntityStateBehavior getStateBehavior(Integer id)
	{
		logger.debug("get State Behavior:" + Integer.toString(id));
		
		for (int i = 0; i < this.stateBehaviors.size(); i++){
			MeasuredEntityStateBehavior measuredEntityStateBehavior = this.stateBehaviors.get(i);
			if (measuredEntityStateBehavior.getId().equals(id) ){
				logger.debug("stateBehavior" + measuredEntityStateBehavior.toString());
				return this.stateBehaviors.get(i);
			}
		}
		
		return null;
	}
	
	/**
	 * Gets an state transition by its unique internal identifier
	 * 
	 * @param id	state transition unique internal identifier
	 * 
	 * @return		state transition instance if found, or null
	 */
	public synchronized MeasuredEntityStateTransition getStateTransition(Integer id)
	{
		for (int i = 0; i < this.stateTransitions.size(); i++){
			MeasuredEntityStateTransition measuredEntityStateTransition = this.stateTransitions.get(i);
			if (measuredEntityStateTransition.getId().equals(id) ){
				return this.stateTransitions.get(i);
			}
		}
		
		return null;
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
	 * @param json
	 * @return
	 */
	public synchronized MeasuredEntityBehavior behaviorFromJSON(String json)
	{
		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string and pretty print
		MeasuredEntityBehavior measuredEntityBehavior;
		try {
		
			measuredEntityBehavior = mapper.readValue(json, MeasuredEntityBehavior.class);
			
	    	this.putBehavior(measuredEntityBehavior.getId(), measuredEntityBehavior.getName(), measuredEntityBehavior.getDescr() , measuredEntityBehavior.getBehavior_text()); 
	    	return measuredEntityBehavior;
		    	
		
		} catch (JsonParseException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return null;

	}

	public synchronized MeasuredEntityStateBehavior stateBehaviorFromJSON(String json)
	{
		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string and pretty print
		MeasuredEntityStateBehavior measuredEntityStateBehavior;
		try {
		
			measuredEntityStateBehavior = mapper.readValue(json, MeasuredEntityStateBehavior.class);
			
	    	this.putBehavior(measuredEntityStateBehavior.getId(), measuredEntityStateBehavior.getStateBehaviorType(), measuredEntityStateBehavior.getDescr() , measuredEntityStateBehavior.getBehavior_text()); 
	    	return measuredEntityStateBehavior;
		    	
		
		} catch (JsonParseException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return null;

	}
	
	public synchronized MeasuredEntityBehavior getBehavior(String name)
	{
		logger.debug("behavior:" + name);

		for (int i = 0; i < this.behaviors.size(); i++){
			MeasuredEntityBehavior measuredEntityBehavior = this.behaviors.get(i);
			if (measuredEntityBehavior.getName().compareTo(name) == 0){
				return measuredEntityBehavior;
			}
		}
		return null;
	}

	public synchronized MeasuredEntityBehavior getBehavior(Integer id)
	{
		logger.debug("behavior:" + id);

		for (int i = 0; i < this.behaviors.size(); i++){
			MeasuredEntityBehavior measuredEntityBehavior = this.behaviors.get(i);
			if (measuredEntityBehavior.getId().compareTo(id) == 0){
				return measuredEntityBehavior;
			}
		}
		return null;
	}
	
	public synchronized MeasuredEntityStateBehavior getStateBehavior(String stateBehaviorType)
	{
		logger.debug("State Behavior:" + stateBehaviorType);

		for (int i = 0; i < this.stateBehaviors.size(); i++){
			MeasuredEntityStateBehavior measuredEntityStateBehavior = this.stateBehaviors.get(i);
			if (measuredEntityStateBehavior.getStateBehaviorType().compareTo(stateBehaviorType) == 0){
				return measuredEntityStateBehavior;
			}
		}
		return null;
	}
	
	
	public synchronized void removeBehaviors()
	{
		this.behaviors.clear();
	}

	public synchronized void removeStateBehaviors()
	{
		this.stateBehaviors.clear();
	}

	
	public synchronized void updateEntityConfiguration(MeasuredEntity measuredEntity) {

		logger.debug("Update Entity Configuration - MeasuredEntity" + measuredEntity);
		
		// update behaviors.
		removeBehaviors();
		for ( int i=0; i < measuredEntity.behaviors.size(); i++)
		{
			putBehavior(measuredEntity.behaviors.get(i).getId(),
					     measuredEntity.behaviors.get(i).getName(), 
						   measuredEntity.behaviors.get(i).getDescr(), 
							measuredEntity.behaviors.get(i).getBehavior_text() );			
		}
		
		// update state behaviors.
		removeStateBehaviors();
		for ( int i=0; i < measuredEntity.stateBehaviors.size(); i++)
		{
			putStateBehavior(measuredEntity.stateBehaviors.get(i) );			
		}
		
		if (measuredEntity instanceof Machine){
			
		}
	}

    @JsonIgnore
    public synchronized void startInterval(LocalDateTime dateTime,  MeasuringState newState, ReasonCode rCode) {
    	currentState = newState;
    	currentReason= rCode;
    	startDateTimeStatus = dateTime;
    }
    
    public synchronized void setCurrentReasonCode(ReasonCode rCode){
    	currentReason= rCode;
    }
    
    @JsonIgnore
    public synchronized MeasuringState getCurrentState(){
    	return this.currentState;
    }
    
    @JsonIgnore
    public synchronized ReasonCode getCurrentReason()
    {
    	return this.currentReason;
    }
    
    @JsonIgnore
    public synchronized LocalDateTime getCurrentStatDateTime()
    {
    	return this.startDateTimeStatus;
    }

    public synchronized void addExecutedEntity(ExecutedEntity executedEntity)
    {
    	logger.debug("Measure entity Id:" + getId() + " Adding executed Entity:" + executedEntity.getId());
    	
    	this.executedEntities.put(executedEntity.getId(), executedEntity);
    }
    
    public synchronized void stopExecuteEntities()
    {
    	
    	ProductionOrderManager productionOrderManager;
		try {
			
			productionOrderManager = ProductionOrderManager.getInstance();
    	
			for (Integer id : this.executedEntities.keySet()){
				ProductionOrderFacade productionOrderFacade = productionOrderManager.getFacadeOfPOrderById(id);
				productionOrderFacade.stop();
			}

		} catch (SQLException e) {
			logger.error("error obtaining an production order manager instance :" + e.getMessage());
			e.printStackTrace();
		}
		
    }
    
    @JsonIgnore
    public synchronized ExecutedEntity getExecutedEntity(Integer id){
    	return this.executedEntities.get(id);
    }
    
    public synchronized void removeExecutedEntity(Integer id){
    	this.executedEntities.remove(id);
    }

    @JsonIgnore
    public synchronized ExecutedEntity getCurrentExecutedEntity()
    {
    	
    	for (Integer id : this.executedEntities.keySet()){
    		ExecutedEntity executedEntity = this.executedEntities.get(id);  
    		if (executedEntity.getCurrentState() == MeasuringState.OPERATING)
    			return executedEntity;
    	}
    	
    	return null;
    }
    
    @JsonIgnore
    public synchronized Double getProductionRate(String productionRateId)
    {
    	
    	// First, it obtains the production rate from the executed entities (production order). 
		try {
			for (Integer id : this.executedEntities.keySet()){
				ExecutedEntity executedEntity = this.executedEntities.get(id);  
				if (executedEntity.getCurrentState() == MeasuringState.OPERATING)
				{
					ProductionOrderManager pOrderManager;
					pOrderManager = ProductionOrderManager.getInstance();
					productionRateId = pOrderManager.getFacadeOfPOrderById(id).getProductionRateId();
					AttributeValue value = executedEntity.getAttributeValue(productionRateId);
					if (value == null){
						logger.error(" The attribute: " + productionRateId + " does not exist in the executed entitity:" + id);
						return null;
					} else {
						return (Double) value.getValue();
					}
				}
			}
		} catch (SQLException e) {
			// For this case, we assume that there is no executed entities on the machine.
			logger.error(e.getMessage());
			e.printStackTrace();
		}
    	
    	// Second, If not found in the executed entities, then it gets the data from the measured entity.
    	AttributeValue value = getAttributeValue(productionRateId);
    	if (value == null){
			logger.error(" The attribute: " + productionRateId + " does not exist in the measured entitity:" + getId());
			return null;
    	} else {
    		return (Double) value.getValue();
    	}
    }

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
    
    @JsonIgnore
    public synchronized String getBehaviorText(MeasuringState state, Integer idRazonParada) {
		int behaviorId = 0; 
		for (int i = 0; i < this.stateTransitions.size(); i++){
			MeasuredEntityStateTransition measuredEntityStateTransition = this.stateTransitions.get(i);
			if ((measuredEntityStateTransition.getStateFrom() == state ) && (measuredEntityStateTransition.getResonCode() == idRazonParada.intValue() ) ) {
				behaviorId = this.stateTransitions.get(i).getBehavior();
				break;
			}
		}
		
		if (behaviorId > 0){
			if (this.getStateBehavior(behaviorId) != null)
				return this.getStateBehavior(behaviorId).getBehavior_text();
		}
		
		return null;
	}

    @JsonIgnore
    public synchronized AttributeValue getAttributeFromExecutedObject(String attributeId) {
		logger.debug("Starting getAttributeFromExecutedObject - measure entity id:" + getId() + " attribute:" + attributeId + " executed entities:" + this.executedEntities.size());
		
		for (Integer id : this.executedEntities.keySet()){
			logger.debug("Executed Object:" + id);
			ExecutedEntity executedEntity = this.executedEntities.get(id);
			if (executedEntity.getCurrentState() == MeasuringState.OPERATING){
				return executedEntity.getAttributeValue(attributeId);
			}
		}
		return null;
	}
	
    @JsonIgnore
    public synchronized String getCanonicalIdentifier()
	{
		return null;
	}

	/**
	 * This function verifies if the current interval state should be calculated and saved 
	 * @return
	 */
	public synchronized boolean startNewInterval() {
		
		if (getCurrentStatDateTime().plusSeconds(getMaxTimeForInterval()).isBefore(LocalDateTime.now()))
			return true;
		
		return false;
	}
	
    @JsonIgnore
    public synchronized List<AttributeValue> getAttributeValueList(){
    	return attributeValues;
    }

    @JsonIgnore
    public synchronized boolean registerAttributeValue(AttributeValue value){
    	return attributeValues.add(value);
    }

    @JsonIgnore
	public synchronized Double getConversion1(String conversion1) {
    	// First, it obtains the production rate from the executed entities (production order). 
		try {
			for (Integer id : this.executedEntities.keySet()){
				ExecutedEntity executedEntity = this.executedEntities.get(id);  
				if (executedEntity.getCurrentState() == MeasuringState.OPERATING)
				{
					ProductionOrderManager pOrderManager;
					pOrderManager = ProductionOrderManager.getInstance();
					conversion1 = pOrderManager.getFacadeOfPOrderById(id).getConversion1();
					AttributeValue value = executedEntity.getAttributeValue(conversion1);
					if (value == null){
						logger.error(" The attribute: " + conversion1 + " does not exist in the executed entitity:" + id);
						return null;
					} else {
						return (Double) value.getValue();
					}
				}
			}
		} catch (SQLException e) {
			// For this case, we assume that there is no executed entities on the machine.
			logger.error(e.getMessage());
			e.printStackTrace();
		}
    	
    	// Second, If not found in the executed entities, then it gets the data from the measured entity.
    	AttributeValue value = getAttributeValue(conversion1);
    	if (value == null){
			logger.error(" The attribute: " + conversion1 + " does not exist in the measured entitity:" + getId());
			return null;
    	} else {
    		return (Double) value.getValue();
    	}

	}

    @JsonIgnore
	public synchronized Double getConversion2(String conversion2) {
    	// First, it obtains the production rate from the executed entities (production order). 
		try {
			for (Integer id : this.executedEntities.keySet()){
				ExecutedEntity executedEntity = this.executedEntities.get(id);  
				if (executedEntity.getCurrentState() == MeasuringState.OPERATING)
				{
					ProductionOrderManager pOrderManager;
					pOrderManager = ProductionOrderManager.getInstance();
					conversion2 = pOrderManager.getFacadeOfPOrderById(id).getConversion2();
					AttributeValue value = executedEntity.getAttributeValue(conversion2);
					if (value == null){
						logger.error(" The attribute: " + conversion2 + " does not exist in the executed entitity:" + id);
						return null;
					} else {
						return (Double) value.getValue();
					}
				}
			}
		} catch (SQLException e) {
			// For this case, we assume that there is no executed entities on the machine.
			logger.error(e.getMessage());
			e.printStackTrace();
		}
    	
    	// Second, If not found in the executed entities, then it gets the data from the measured entity.
    	AttributeValue value = getAttributeValue(conversion2);
    	if (value == null){
			logger.error(" The attribute: " + conversion2 + " does not exist in the measured entitity:" + getId());
			return null;
    	} else {
    		return (Double) value.getValue();
    	}

	}

	public synchronized void removeStateBehavior(Integer behaviorId) {

		for (int i = 0; i < this.stateBehaviors.size(); i++){
			MeasuredEntityStateBehavior measuredEntityStateBehavior = this.stateBehaviors.get(i);
			if (measuredEntityStateBehavior.getId().compareTo(behaviorId) == 0){
				this.stateBehaviors.remove(i);
				break;
			}
		}		

	}

	public synchronized void removeStateTransition(Integer transitionId) {

		for (int i = 0; i < this.stateTransitions.size(); i++){
			MeasuredEntityStateTransition transition = this.stateTransitions.get(i);
			if (transition.getId().compareTo(transitionId) == 0){
				this.stateTransitions.remove(i);
				break;
			}
		}		
	}
}
