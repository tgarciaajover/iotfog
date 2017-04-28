package com.advicetec.measuredentitity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.json.JSONArray;

import com.advicetec.configuration.LocalDateTimeDeserializer;
import com.advicetec.configuration.LocalDateTimeSerializer;
import com.advicetec.core.Attribute;
import com.advicetec.core.TimeInterval;
import com.advicetec.persistence.MeasureAttributeValueCache;
import com.advicetec.persistence.StateIntervalCache;

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
public abstract class MeasuredEntity 
{
	@JsonProperty("id")
	protected String id;
	
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
    
	
    @JsonIgnore
    protected LocalDateTime startDateTimeStatus;	// last time interval
      
    @JsonIgnore
    protected List<AttributeMeasuredEntity> attributes;
    
    
    public MeasuredEntity(String id, MeasuredEntityType type) 
    {
		super();
		this.id = id;
		this.type = type;
		createDate = LocalDateTime.now();
		behaviors = new ArrayList<MeasuredEntityBehavior>();
		startDateTimeStatus = LocalDateTime.now();
		//measures = new HashMap<String, MeasuredAttributeValue>();
		//intervals = new HashMap<String, StateInterval>();
		attributes = new ArrayList<AttributeMeasuredEntity>();
	}

    public String getId()
    {
    	return id;
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
    	return this.id.equals(other.getId());
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

	public synchronized void putBehavior(String name, String descr, String behavior_text)
	{
		boolean inserted = false; 
		for (int i = 0; i < this.behaviors.size(); i++){
			MeasuredEntityBehavior measuredEntityBehavior = this.behaviors.get(i);
			if (measuredEntityBehavior.getName().compareTo(name) == 0){
				MeasuredEntityBehavior measuredEntityBehavior2 = new MeasuredEntityBehavior(name);
				measuredEntityBehavior2.setDescr(descr);
				measuredEntityBehavior2.setBehaviorText(behavior_text);
				this.behaviors.remove(i);
				this.behaviors.add(measuredEntityBehavior2);
				inserted = true;
				break;
			}
		}
		
		if (inserted == false){
			MeasuredEntityBehavior measuredEntityBehavior2 = new MeasuredEntityBehavior(name);
			measuredEntityBehavior2.setDescr(descr);
			measuredEntityBehavior2.setBehaviorText(behavior_text);
			this.behaviors.add(measuredEntityBehavior2);
		}
	}
	
	public synchronized String getBehaviorText(String name)
	{
		for (int i = 0; i < this.behaviors.size(); i++){
			MeasuredEntityBehavior measuredEntityBehavior = this.behaviors.get(i);
			if (measuredEntityBehavior.getName().compareTo(name) == 0){
				return measuredEntityBehavior.getBehavior_text();
			}
		}
		return null;
	}
	
	public synchronized void removeBehaviors()
	{
		this.behaviors.clear();
	}
	
}
