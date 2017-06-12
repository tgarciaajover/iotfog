package com.advicetec.applicationAdapter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.configuration.ConfigurationObject;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;
import com.advicetec.measuredentitity.AttributeMeasuredEntity;
import com.advicetec.measuredentitity.ExecutedEntity;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.MeasuredEntityBehavior;
import com.advicetec.measuredentitity.MeasuredEntityStateBehavior;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.MeasuringState;

public class ProductionOrder extends ExecutedEntity
{

	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	private LocalDateTime createDate;

	@JsonIgnore
	MeasuredEntityType type;

	@JsonProperty("last_updttm") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	private LocalDateTime lastDateTime;

    @JsonIgnore
    protected LocalDateTime startDateTimeStatus;	// start of the current interval.

    @JsonIgnore
    private MeasuringState currentState; // state of the current interval.

    private ReasonCode currentReason; // the reason code for the state. 
    
    @JsonIgnore
    protected List<Attribute> attributes;

    @JsonIgnore
    protected List<AttributeValue> attributeValues;
    
    public ProductionOrder(@JsonProperty("id") Integer id) 
    {
		super(id);
		startDateTimeStatus = LocalDateTime.now();
		currentState = MeasuringState.SCHEDULEDOWN;
		currentReason = null;
		attributes = new ArrayList<Attribute>();
		attributeValues = new ArrayList<AttributeValue>();
		type = MeasuredEntityType.JOB;
	}

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
    
    public AttributeValue getAttributeValue(String name){
    	  
		for (int i = 0; i < this.attributeValues.size(); i++){
			AttributeValue attr = this.attributeValues.get(i);
			if ((attr.getAttr().getName()).compareTo(name) == 0){
				return attr;
			}
		}
    	
		return null;
    }
    
    @JsonIgnore
    public List<Attribute> getAttributeList(){
    	return attributes;
    }
    
    public boolean registerAttribute(Attribute attrribute){
    	return attributes.add(attrribute);
    }
    
    @JsonIgnore
    public boolean equals(ProductionOrder other){
    	
    	// we said that a production order is equal to another if both have the same 
    	// attributes and their attribute values are also equal. 
    	if (getId() != other.getId())
    		return false;
    	
    	// Check that both orders have the same attributes.  
		for (int i = 0; i < this.attributes.size(); i++){
			Attribute attr = other.getAttribute(this.attributes.get(i).getName());
			if ((attr == null) || (attr.equals(this.attributes.get(i)) == false)){
				return false;
			}
		}

    	// Check that both orders have the same attributes values.  
		for (int i = 0; i < this.attributeValues.size(); i++){
			AttributeValue attr = other.getAttributeValue(this.attributes.get(i).getName());
			if ((attr == null) || (attr.equals(this.attributeValues.get(i)) == false)){
				return false;
			}
		}

    	return true;
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
    public MeasuredEntityType getType(){
    	return type;
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
}
