package com.advicetec.measuredentitity;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.applicationAdapter.ProductionOrderManager;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.Entity;
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
public abstract class MeasuredEntity extends Entity 
{
	static final Logger logger = LogManager.getLogger(MeasuredEntity.class.getName());


	/**
	 * Code assigned to the measured entity to make easier the user interface. 
	 */
	@JsonProperty("code")
	protected String code;

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

	
	@JsonIgnore
	protected EntityState state;
	
	/**
	 * Set of behavior registered to the measured entity 
	 * @see MeasuredEntityBehavior
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
	 * List of executed entities being processed in this measured entity.
	 * Only one of the executed entities must be OPERATING at time.
	 */
	@JsonIgnore
	protected Map<Integer, ExecutedEntity> executedEntities;


	/**
	 * Constructor for the class 
	 * @param id	internal unique identifier
	 * @param type	type of measured entity
	 */
	public MeasuredEntity( Integer id, MeasuredEntityType type) 
	{
		super(id, type);
		
		this.createDate = LocalDateTime.now();
		this.behaviors = new ArrayList<MeasuredEntityBehavior>();
		this.state = new EntityState(MeasuringState.SYSTEMDOWN, null, LocalDateTime.now());
		
		this.stateBehaviors = new ArrayList<MeasuredEntityStateBehavior>();
		this.stateTransitions = new ArrayList<MeasuredEntityStateTransition>();
		this.executedEntities = new ConcurrentHashMap<Integer, ExecutedEntity>();

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
	 * Denotes if another measured entity is equals to this object. 
	 * This method compares only their corresponding ids.
	 * 
	 * @param other	the measured entity to compare
	 * @return TRUE if this and the given objects are equal, FALSE otherwise.
	 */
	public synchronized boolean equals(Object o){
		
		if (o instanceof MeasuredEntity) {
			MeasuredEntity other = (MeasuredEntity) o;
			return getId().equals( other.getId());
		} else {
			return false;
		}
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
	 * Serialize the measure entity to JSON string.
	 * 
	 * @return the JSON string representation of this object.
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
	 * Adds a behavior to this measured entity.
	 * 
	 * @param id				internal behavior identifier  
	 * @param name				name of the behavior
	 * @param descr				behavior description
	 * @param behavior_text		behavior text
	 */
	public synchronized void putBehavior(
			Integer id,String name, String descr, String behavior_text)
	{
		boolean inserted = false; 
		for (int i = 0; i < this.behaviors.size() && !inserted; i++){
			MeasuredEntityBehavior measuredEntityBehavior = this.behaviors.get(i);
			// if the behavior list already contains the given behavior name 
			if (measuredEntityBehavior.getName().compareTo(name) == 0){
				// updates values
				MeasuredEntityBehavior measuredEntityBehavior2 = new MeasuredEntityBehavior(id, name);
				measuredEntityBehavior2.setDescr(descr);
				measuredEntityBehavior2.setBehaviorText(behavior_text);
				// remove the previous
				this.behaviors.remove(i);
				this.behaviors.add(measuredEntityBehavior2);
				inserted = true;
				break;
			}
		}
		// if the behavior list does not contain the given behavior name
		if (inserted == false){
			// create the new behavior
			MeasuredEntityBehavior measuredEntityBehavior2 = new MeasuredEntityBehavior(id, name);
			measuredEntityBehavior2.setDescr(descr);
			measuredEntityBehavior2.setBehaviorText(behavior_text);
			// adding to the list
			this.behaviors.add(measuredEntityBehavior2);
		}
	}

	/**
	 * Deletes a behavior from the list of this measured entity.
	 *  
	 * @param id 	internal behavior identifier.
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
	 * Adds a new state behavior to the measured entity.
	 * 
	 * @param measuredEntityStateBehavior	Measured state behavior entity to add.
	 */
	public synchronized void putStateBehavior(MeasuredEntityStateBehavior measuredEntityStateBehavior)
	{
		logger.debug("Put State Behavior" + Integer.toString(this.stateBehaviors.size()));

		for (int i = 0; i < this.stateBehaviors.size(); i++){
			MeasuredEntityStateBehavior measuredEntityStateBehavior2 = this.stateBehaviors.get(i);
			if (measuredEntityStateBehavior2.getId().equals(measuredEntityStateBehavior.getId())){
				// if the state behavior is in the list, removes it.
				logger.debug("Removed measured state behavior with id:"+measuredEntityStateBehavior2.getId());
				this.stateBehaviors.remove(i);
				break;
			}
		}
		// after ensure there is not the same behavior in the list, insert it.
		this.stateBehaviors.add(measuredEntityStateBehavior);

		for (int i = 0; i < this.stateBehaviors.size(); i++){
			logger.debug("statebehavior:" + this.stateBehaviors.get(i).toString());
		}
		logger.debug("Method end. Num State Behavior" + Integer.toString(this.stateBehaviors.size()));
	}

	/**
	 * Adds a new Scheduled Event to this measured entity.  
	 * 	
	 * @param measuredEntityScheduledEvent	scheduled event to be added. 
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
	public synchronized void putStateTransition(Integer id, MeasuringState stateFrom, 
			Integer reasonCodeFrom, Integer behavior, LocalDateTime createDate)
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
	 * Inserts a behavior from JSON object.
	 *  
	 * @param json  Json object representing the behavior to insert.
	 * 
	 * @return  The MeasuredEntityBehavior object that was created.
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

	/**
	 * Creates a state behavior object from JSON.
	 * 
	 * @param json Json Object representing the behavior to insert.
	 * 
	 * @return The measured entity state behavior that was created.
	 */
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

	/**
	 * Get the behavior object from its name
	 *  
	 * @param name name of the behavior to find
	 * 
	 * @return  Measured Entity Behavior Object or null if the behavior was not found. 
	 */
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

	/**
	 * Get the behavior object from its internal identifier
	 * 
	 * @param id  internal identifier of the behavior object to return
	 * 
	 * @return Measured Entity Behavior Object or null if the behavior was not found. 
	 */
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

	/**
	 * Get the STATE behavior object from the state behavior type
	 * 
	 * @param stateBehaviorType  state behavior type to return 
	 *   
	 * @return  Measured Entity State Behavior or null if the behavior was not found. 
	 */
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

	/**
	 * Remove all behaviors registered in the measured entity
	 */
	public synchronized void removeBehaviors()
	{
		this.behaviors.clear();
	}

	/**
	 * Remove all state behavior registered in the measured entity 
	 */
	public synchronized void removeStateBehaviors()
	{
		this.stateBehaviors.clear();
	}

	/**
	 * Update the measured entity copy the information from another measured entity
	 * 
	 * @param measuredEntity  measured entity definition to copy.
	 */
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

	}

	/**
	 * Registers the start of a new interval in the measured entity
	 *   
	 * @param dateTime 	date and time when this interval should start
	 * @param newState	state given to the machine 
	 * @param rCode		Reason code for the interval
	 */
	@JsonIgnore
	public synchronized void startInterval(LocalDateTime dateTime,  MeasuringState newState, ReasonCode rCode) {
		this.state.update(newState, rCode, dateTime);
	}

	/**
	 * Establishes the reason code for the current interval
	 * 
	 * @param rCode  reason code to set.
	 */
	public synchronized void setCurrentReasonCode(ReasonCode rCode){
		this.state.setReason(rCode);
	}

	/**
	 * Gets the state for the current interval.
	 * 
	 * @return current interval state.
	 */
	@JsonIgnore
	public synchronized MeasuringState getCurrentState(){
		return this.state.getState();
	}

	/**
	 * Gets the reason code for the current interval. 
	 * 
	 * @return	 current interval reason
	 */
	@JsonIgnore
	public synchronized ReasonCode getCurrentReason()
	{
		return this.state.getReason();
	}

	/**
	 * Gets the date and time when the current interval started 
	 * 
	 * @return The date and time when the current interval started
	 */
	@JsonIgnore
	public synchronized LocalDateTime getCurrentStatDateTime()
	{
		return this.state.getStartDateTimeStatus();
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
	
	
	/**
	 * Adds an executed entity to this measured entity
	 * 
	 * @param executedEntity executed entity to add.
	 */
	public synchronized void addExecutedEntity(ExecutedEntity executedEntity)
	{
		logger.info("Measure entity Id:" + getId() + " Adding executed Entity:" + executedEntity.getId());

		this.executedEntities.put(executedEntity.getId(), executedEntity);
		
		logger.info("Num of executed entities being executed: ",  this.executedEntities.size());
	}

	/**
	 *  Stops all executed entities being processed in this measured entity 
	 */
	public synchronized void stopExecuteEntities()
	{
		ProductionOrderManager productionOrderManager;
		try {
			productionOrderManager = ProductionOrderManager.getInstance();
			for (Integer id : this.executedEntities.keySet()){
				// stops all facades
				ExecutedEntityFacade productionOrderFacade = productionOrderManager.getFacadeOfPOrderById(id);
				productionOrderFacade.stop(this.getId());
			}

		} catch (SQLException e) {
			logger.error("error obtaining an production order manager instance :" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Gets the executed entity being performed by its unique identifier 
	 * 
	 * @param id	unique internal identifier of the executed entity to return. 
	 * @return		executed entity object 
	 */
	@JsonIgnore
	public synchronized ExecutedEntity getExecutedEntity(Integer id){
		return this.executedEntities.get(id);
	}

	/**
	 * Removes the executed entity with the identifier given as parameter
	 * 
	 * @param id unique internal identifier of the executed entity.
	 */
	public synchronized void removeExecutedEntity(Integer id){
		this.executedEntities.remove(id);
	}

	/**
	 * Obtains the executed entity being processed in the measured entity 
	 * 
	 * @return  current executed entity being processed. 
	 * If any is OPERATING, otherwise returns NULL.
	 */
	@JsonIgnore
	public synchronized ExecutedEntity getCurrentExecutedEntity()
	{
		logger.debug("Number of executed entities included :" + this.executedEntities.size());
		
		for (Integer id : this.executedEntities.keySet()){
			ExecutedEntity executedEntity = this.executedEntities.get(id); 
			logger.debug("Executed entity started: " + id);
			
			// only ONE executed entity must be in OPERATING state at any time. During execution, the system can go off. In that case,
			// the system register the production order in shutdown state.
			
			if ((executedEntity.getCurrentState(getId()) == MeasuringState.OPERATING) 
				 || (executedEntity.getCurrentState(getId()) == MeasuringState.SYSTEMDOWN) 
					|| (executedEntity.getCurrentState(getId()) == MeasuringState.INITIALIZING)
					 || (executedEntity.getCurrentState(getId()) == MeasuringState.UNSCHEDULEDOWN) )
			{
				return executedEntity;
			}
		}

		return null;
	}

	/**
	 * Gets the production rate from this measured entity.
	 * 
	 * @param productionRateId  production rate field identifier
	 * @return the production rate assigned, if the field is not found, then returns NULL.
	 */
	@JsonIgnore
	public synchronized Double getProductionRate(String productionRateId)
	{
		// First, it obtains the production rate from the executed entities (production order). 
		try {
			for (Integer id : this.executedEntities.keySet()){
				ExecutedEntity executedEntity = this.executedEntities.get(id);  
				if (executedEntity.getCurrentState(getId()) == MeasuringState.OPERATING)
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
			logger.error("There is no executed entities on the machine."+e.getMessage());
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


	/**
	 * Gets the status behavior by state and reason code 
	 * 
	 * @param state  			measuring entity state  
	 * @param idRazonParada		reason code
	 * @return	state behavior or null if not found.
	 */
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

	/**
	 * Gets an attribute from the executed object being executed
	 * 
	 * @param  Name of the attribute to get.
	 * 
	 * @return attribute in the executed entity or null if not found. 
	 */
	@JsonIgnore
	public synchronized AttributeValue getAttributeFromExecutedObject(String attributeId) {
		logger.debug("Starting getAttributeFromExecutedObject - measure entity id:" + getId() + " attribute:" + attributeId + " executed entities:" + this.executedEntities.size());

		for (Integer id : this.executedEntities.keySet()){
			logger.debug("Executed Object:" + id);
			ExecutedEntity executedEntity = this.executedEntities.get(id);
			if (executedEntity.getCurrentState(getId()) == MeasuringState.OPERATING){
				return executedEntity.getAttributeValue(attributeId);
			}
		}
		return null;
	}


	/**
	 * Gets the conversion one from the measured entity
	 * 
	 * @param conversion1 field name of the conversion one
	 * 
	 * @return conversion one or null if not found as attribute.
	 */
	@JsonIgnore
	public synchronized Double getConversion1(String conversion1) {
		// First, it obtains the production rate from the executed entities (production order). 
		try {
			for (Integer id : this.executedEntities.keySet()){
				ExecutedEntity executedEntity = this.executedEntities.get(id);  
				if (executedEntity.getCurrentState(getId()) == MeasuringState.OPERATING)
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

	/**
	 * Gets the conversion two from the measured entity
	 * 
	 * @param conversion2  field name of the conversion two
	 * 
	 * @return  conversion two or null if not found as attribute.
	 */
	@JsonIgnore
	public synchronized Double getConversion2(String conversion2) {
		// First, it obtains the production rate from the executed entities (production order). 
		try {
			for (Integer id : this.executedEntities.keySet()){
				ExecutedEntity executedEntity = this.executedEntities.get(id);  
				if (executedEntity.getCurrentState(getId()) == MeasuringState.OPERATING)
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

	/**
	 * Removes the state behavior from the measured entity by its unique identifier 
	 * 
	 * @param behaviorId  unique identifier of the state behavior
	 */
	public synchronized void removeStateBehavior(Integer behaviorId) {

		for (int i = 0; i < this.stateBehaviors.size(); i++){
			MeasuredEntityStateBehavior measuredEntityStateBehavior = this.stateBehaviors.get(i);
			if (measuredEntityStateBehavior.getId().compareTo(behaviorId) == 0){
				this.stateBehaviors.remove(i);
				break;
			}
		}		

	}

	/**
	 * Removes an state transition from the measured entity by its unique identifier 
	 * 
	 * @param transitionId	unique identifier of the state transition
	 */
	public synchronized void removeStateTransition(Integer transitionId) {

		for (int i = 0; i < this.stateTransitions.size(); i++){
			MeasuredEntityStateTransition transition = this.stateTransitions.get(i);
			if (transition.getId().compareTo(transitionId) == 0){
				this.stateTransitions.remove(i);
				break;
			}
		}		
	}
	
	/**
	 * Creates and returns a MessageAttributeValue
	 *  
	 * @param attribute  Attribute for which is created a new value  
	 * @param value 	 The value of this new measured attribute value
	 * @param timeStamp  Date and time when the new measured attribute value is created.
	 * 
	 * @return the new measured attribute value created.
	 */
	public synchronized MeasuredAttributeValue getMeasureAttributeValue(
			Attribute attribute, Object value, LocalDateTime timeStamp)
	{
		return new MeasuredAttributeValue(attribute, value, getId(), getType(), timeStamp);
	}
	
}
