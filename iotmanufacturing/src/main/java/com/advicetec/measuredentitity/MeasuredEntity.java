package com.advicetec.measuredentitity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

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
public abstract class MeasuredEntity 
{
    protected String id;
    protected MeasuredEntityType type; 
    protected LocalDateTime startDateTimeStatus;	// last time interval
    
    //protected MeasureAttributeValueStore measures;  // TODO: Should be a cache with write.
    // stores the states and their intervals.
    //protected Map<String, StateInterval> intervals;		 // TODO: Should be a cache with write.
    
    protected List<AttributeMeasuredEntity> attributes;
    
    
    public MeasuredEntity(String id, MeasuredEntityType type) 
    {
		super();
		this.id = id;
		this.type = type;
		startDateTimeStatus = LocalDateTime.now();
		//measures = new HashMap<String, MeasuredAttributeValue>();
		//intervals = new HashMap<String, StateInterval>();
		attributes = new ArrayList<AttributeMeasuredEntity>();
	}

    public String getId()
    {
    	return id;
    }
    
    public MeasuredEntityType getType()
    {
    	return this.type;
    }
    
	public void getTimePerStatus(MeasuringStatus status, TimeInterval interval)
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
}
