package com.advicetec.measuredentitity;

import java.io.IOException;
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

import com.advicetec.configuration.ConfigurationObject;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.core.Attribute;
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
	    @Type(value = ProductionJob.class, name = "J") })
public abstract class MeasuredEntity extends ConfigurationObject 
{
	
	static final Logger logger = LogManager.getLogger(MeasuredEntity.class.getName()); 
	
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
    protected List<AttributeMeasuredEntity> attributes;
    
    
    public MeasuredEntity(@JsonProperty("id") Integer id, MeasuredEntityType type) 
    {
		super(id);
		this.type = type;
		createDate = LocalDateTime.now();
		behaviors = new ArrayList<MeasuredEntityBehavior>();
		startDateTimeStatus = LocalDateTime.now();
		currentState = MeasuringState.SCHEDULEDOWN;
		currentReason = null;

		attributes = new ArrayList<AttributeMeasuredEntity>();
		stateBehaviors = new ArrayList<MeasuredEntityStateBehavior>();
		executedEntities = new HashMap<Integer, ExecutedEntity>();
		
	}

    
    public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public MeasuredEntityType getType()
    {
    	return this.type;
    }
    
	public void getTimePerStatus(MeasuringState status, TimeInterval interval)
    {
    	
    }
    
	/**
	 * Creates and returns a MessageAttributeValue 
	 * @param attribute The atribute
	 * @param value The value.
	 * @param timeStamp 
	 * @return 
	 */
    public MeasuredAttributeValue getMeasureAttributeValue(Attribute attribute, Object value, LocalDateTime timeStamp)
    {
    	return new MeasuredAttributeValue(attribute, value, getId(), getType(), timeStamp);
    }
    
    @JsonIgnore
    public List<AttributeMeasuredEntity> getAttributeList(){
    	return attributes;
    }
    
    public boolean registerMeasureEntityAttibute(AttributeMeasuredEntity attrMeasureEntity){
    	return attributes.add(attrMeasureEntity);
    }
    
    public boolean equals(MeasuredEntity other){
    	return getId() == other.getId();
    }

	public void getStateByInterval(TimeInterval timeInterval) {
		// TODO Auto-generated method stub
		
	}
	
	public LocalDateTime getCreateDate() {
		return createDate;
	}
	
	public void setCreateDate(LocalDateTime create_date) {
		this.createDate = create_date;
	}

	public String getDescr() {
		return descr;
	}
	
	public void setDescr(String descr) {
		this.descr = descr;
	}	
	
	public String toJson()
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
		boolean inserted = false; 
		for (int i = 0; i < this.stateBehaviors.size(); i++){
			MeasuredEntityStateBehavior measuredEntityStateBehavior = this.stateBehaviors.get(i);
			if (measuredEntityStateBehavior.getId() == id){
				MeasuredEntityStateBehavior measuredEntityStateBehavior2 = new MeasuredEntityStateBehavior(id, stateBehaviorType);
				measuredEntityStateBehavior2.setDescr(descr);
				measuredEntityStateBehavior2.setBehaviorText(behavior_text);
				this.stateBehaviors.remove(i);
				this.stateBehaviors.add(measuredEntityStateBehavior2);
				inserted = true;
				break;
			}
		}
		
		if (inserted == false){
			MeasuredEntityStateBehavior measuredEntityStateBehavior2 = new MeasuredEntityStateBehavior(id, stateBehaviorType);
			measuredEntityStateBehavior2.setDescr(descr);
			measuredEntityStateBehavior2.setBehaviorText(behavior_text);
			this.stateBehaviors.add(measuredEntityStateBehavior2);
		}
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
			if (measuredEntityStateTransition.getId() == id ){
				this.stateTransitions.remove(i);
				break;
			}
		}
		
		this.stateTransitions.add(measuredEntityStateTransitionNew);
		
	}
	
	public synchronized String getBehaviorText(String name)
	{
		logger.debug("behavior:" + name);
		
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
	
	public synchronized MeasuredEntityStateTransition getStateTransition(Integer id){

		for (int i = 0; i < this.stateTransitions.size(); i++){
			MeasuredEntityStateTransition measuredEntityStateTransition = this.stateTransitions.get(i);
			if (measuredEntityStateTransition.getId() == id ){
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
    public void startInterval(MeasuringState newState, ReasonCode rCode) {
    	currentState = newState;
    	currentReason= rCode;
    	startDateTimeStatus = LocalDateTime.now();
    }
    
    @JsonIgnore
    public MeasuringState getCurrentState(){
    	return this.currentState;
    }
    
    @JsonIgnore
    public ReasonCode getCurrentReason(){
    	return this.currentReason;
    }
    
    @JsonIgnore
    public LocalDateTime getCurrentStatDateTime(){
    	return this.startDateTimeStatus;
    }

    public void addExecutedEntity(ExecutedEntity executedEntity){
    	this.executedEntities.put(executedEntity.getId(), executedEntity);
    }
    
    public ExecutedEntity getExecutedEntity(Integer id){
    	return this.executedEntities.get(id);
    }
    
    public void removeExecutedEntity(Integer id){
    	this.executedEntities.remove(id);
    }
}
