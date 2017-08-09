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

/**
 * Represents the resulting the measuring process.
 * It stores the values into cache.
 * 
 * @author user
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
	static Integer MAX_INTERVAL_TIME = 300; // Value measured in seconds. 
	
	@JsonProperty("code")
	protected String code;
	
	@JsonIgnore
	protected MeasuredEntityType type; 

	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	private LocalDateTime createDate;

	@JsonProperty("descr")
	protected String descr;

	@JsonProperty("behaviors")
	protected List<MeasuredEntityBehavior> behaviors;
    
	@JsonProperty("statebehaviors")
	protected List<MeasuredEntityStateBehavior> stateBehaviors;
	
	@JsonProperty("statebehaviors")
	protected List<MeasuredEntityStateTransition> stateTransitions;
	
    @JsonIgnore
    protected LocalDateTime startDateTimeStatus;	// last time interval
    
    @JsonIgnore
    protected MeasuringState currentState;	// current state

    @JsonIgnore
    protected ReasonCode currentReason;	// current Reason Code for the Status.

    @JsonIgnore
    protected Map<Integer, ExecutedEntity> executedEntities;
    
    @JsonIgnore
    protected List<Attribute> attributes;

    @JsonIgnore
    protected List<AttributeValue> attributeValues;
    
    @JsonIgnore
    protected Integer maxTimeForInterval;
    
    
    public MeasuredEntity(@JsonProperty("id") Integer id, MeasuredEntityType type) 
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
		
	}

    public synchronized String getCode() {
		return code;
	}

	public synchronized void setCode(String code) {
		this.code = code;
	}

	public synchronized MeasuredEntityType getType()
    {
    	return this.type;
    }
    	
	public synchronized Integer getMaxTimeForInterval() {
		return maxTimeForInterval;
	}

	public synchronized void setMaxTimeForInterval(Integer maxTimeForInterval) {
		this.maxTimeForInterval = maxTimeForInterval;
	}
    
	/**
	 * Creates and returns a MessageAttributeValue 
	 * @param attribute The atribute
	 * @param value The value.
	 * @param timeStamp 
	 * @return 
	 */
    public synchronized MeasuredAttributeValue getMeasureAttributeValue(Attribute attribute, Object value, LocalDateTime timeStamp)
    {
    	return new MeasuredAttributeValue(attribute, value, getId(), getType(), timeStamp);
    }
    
    @JsonIgnore
    public synchronized List<Attribute> getAttributeList(){
    	return attributes;
    }
    
    public synchronized boolean registerAttribute(Attribute attrMeasureEntity){
    	return attributes.add(attrMeasureEntity);
    }
    
    public synchronized boolean equals(MeasuredEntity other){
    	return getId().equals( other.getId());
    }
	
	public synchronized LocalDateTime getCreateDate() {
		return createDate;
	}
	
	public synchronized void setCreateDate(LocalDateTime create_date) {
		this.createDate = create_date;
	}

	public synchronized String getDescr() {
		return descr;
	}
	
	public synchronized void setDescr(String descr) {
		this.descr = descr;
	}	
	
	public synchronized String toJson()
	{
		ObjectMapper mapper = new ObjectMapper();
			
		String jsonInString=null;
		try {
			
			jsonInString = mapper.writeValueAsString(this);
			
			
		} catch (JsonGenerationException e) {
			// TODO: log the error
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return jsonInString;
	}

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

	public synchronized void putStateBehavior(Integer id, String stateBehaviorType, String descr, String behavior_text)
	{
		logger.debug("Put State Behavior" + Integer.toString(this.stateBehaviors.size()));
		
		MeasuredEntityStateBehavior measuredEntityStateBehavior2 = new MeasuredEntityStateBehavior(id, stateBehaviorType);
		measuredEntityStateBehavior2.setDescr(descr);
		measuredEntityStateBehavior2.setBehaviorText(behavior_text);

		for (int i = 0; i < this.stateBehaviors.size(); i++){
			MeasuredEntityStateBehavior measuredEntityStateBehavior = this.stateBehaviors.get(i);
			if (measuredEntityStateBehavior.getId().equals(id)){
				logger.debug("removed element");
				this.stateBehaviors.remove(i);
				break;
			}
		}
		
		this.stateBehaviors.add(measuredEntityStateBehavior2);

		for (int i = 0; i < this.stateBehaviors.size(); i++){
			logger.debug("statebehavior:" + this.stateBehaviors.get(i).toString());
		}
		
		logger.debug("Method end. Num State Behavior" + Integer.toString(this.stateBehaviors.size()));
	}
	
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
			putStateBehavior(measuredEntity.stateBehaviors.get(i).getId(),
					     measuredEntity.stateBehaviors.get(i).getStateBehaviorType(), 
						   measuredEntity.stateBehaviors.get(i).getDescr(), 
							measuredEntity.stateBehaviors.get(i).getBehavior_text() );			
		}
		
		if (measuredEntity instanceof Machine){
			
		}
	}

    @JsonIgnore
    public void startInterval(LocalDateTime dateTime,  MeasuringState newState, ReasonCode rCode) {
    	currentState = newState;
    	currentReason= rCode;
    	startDateTimeStatus = dateTime;
    }
    
    @JsonIgnore
    public MeasuringState getCurrentState(){
    	return this.currentState;
    }
    
    @JsonIgnore
    public ReasonCode getCurrentReason()
    {
    	return this.currentReason;
    }
    
    @JsonIgnore
    public LocalDateTime getCurrentStatDateTime()
    {
    	return this.startDateTimeStatus;
    }

    public void addExecutedEntity(ExecutedEntity executedEntity)
    {
    	logger.debug("Measure entity Id:" + getId() + " Adding executed Entity:" + executedEntity.getId());
    	
    	this.executedEntities.put(executedEntity.getId(), executedEntity);
    }
    
    public void stopExecuteEntities()
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
    
    public ExecutedEntity getExecutedEntity(Integer id){
    	return this.executedEntities.get(id);
    }
    
    public void removeExecutedEntity(Integer id){
    	this.executedEntities.remove(id);
    }
    
    public Double getProductionRate(String productionRateId)
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
						logger.error(" The attribute: " + productionRateId + "does not exist in the executed entitity:" + id);
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
			logger.error(" The attribute: " + productionRateId + "does not exist in the measured entitity:" + getId());
			return null;
    	} else {
    		return (Double) value.getValue();
    	}
    }

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
    
	public String getBehaviorText(MeasuringState state, Integer idRazonParada) {
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

	public AttributeValue getAttributeFromExecutedObject(String attributeId) {
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
	
	public String getCanonicalIdentifier()
	{
		return null;
	}

	// This function verifies if the current interval state should be calculated and saved 
	public boolean startNewInterval() {
		
		if (getCurrentStatDateTime().plusSeconds(getMaxTimeForInterval()).isBefore(LocalDateTime.now()))
			return true;
		
		return false;
	}
	
    @JsonIgnore
    public List<AttributeValue> getAttributeValueList(){
    	return attributeValues;
    }

    @JsonIgnore
    public boolean registerAttributeValue(AttributeValue value){
    	return attributeValues.add(value);
    }

    @JsonIgnore
	public Double getConversion1(String conversion1) {
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
						logger.error(" The attribute: " + conversion1 + "does not exist in the executed entitity:" + id);
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
			logger.error(" The attribute: " + conversion1 + "does not exist in the measured entitity:" + getId());
			return null;
    	} else {
    		return (Double) value.getValue();
    	}

	}

    @JsonIgnore
	public Double getConversion2(String conversion2) {
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
						logger.error(" The attribute: " + conversion2 + "does not exist in the executed entitity:" + id);
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
			logger.error(" The attribute: " + conversion2 + "does not exist in the measured entitity:" + getId());
			return null;
    	} else {
    		return (Double) value.getValue();
    	}

	}

}
