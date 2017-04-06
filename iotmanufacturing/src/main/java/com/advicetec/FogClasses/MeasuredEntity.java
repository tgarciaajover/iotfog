package com.advicetec.FogClasses;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeasuredEntity 
{
    String id;
    MeasuredEntityType type; 
	LocalDateTime startDateTimeStatus;
    
    Map<String, MeasuredAttributeValue> measures;  // TODO: Should be a cache with write.
    Map<String, StatusInterval> intervals;		 // TODO: Should be a cache with write.
    
    List<AttributeMeasuredEntity> attributes;
    
    public MeasuredEntity(String id, MeasuredEntityType type) 
    {
		super();
		this.id = id;
		this.type = type;
		startDateTimeStatus = LocalDateTime.now();
		measures = new HashMap<String, MeasuredAttributeValue>();
		intervals = new HashMap<String, StatusInterval>();
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
    	
    public void registerAttributeValue(Attribute attribute, Object value, LocalDateTime timeStamp)
    {
    	MeasuredAttributeValue measure = 
    			new MeasuredAttributeValue(attribute, value, getId(), getType(), timeStamp);
    	// TODO review parameters
        this.measures.put(measure.getKey(), measure);
    }
    
    public void registerInterval(MeasuringStatus status, ReasonCode reasonCode, TimeInterval interval)
    {
    	StatusInterval interval_tmp = new StatusInterval(status, reasonCode, interval, getId(), getType());
    	this.intervals.put(interval_tmp.getKey(), interval_tmp);
    }
}
