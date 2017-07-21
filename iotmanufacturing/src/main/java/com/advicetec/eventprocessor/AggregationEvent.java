package com.advicetec.eventprocessor;

import com.advicetec.measuredentitity.MeasuredEntityType;

public class AggregationEvent extends Event
{

	// Type of Aggregation Event type
	private AggregationEventType type;

	// Measuring entity for the aggregation.
	private int measuredEntity;
	
	// Establishes the time of measured entity.
	MeasuredEntityType ownerType;

	public AggregationEvent(int measuredEntity, MeasuredEntityType ownerType,
			AggregationEventType type) {
		super(EventType.AGGREGATION_EVENT);
		this.measuredEntity = measuredEntity;
		this.type = type;
		this.ownerType = ownerType;
	}
	
	public AggregationEventType getType() {
		return type;
	}

	public int getEntity() {
		return measuredEntity;
	}
	
	

	public MeasuredEntityType getOwnerType() {
		return ownerType;
	}

	public String getKey(){
		return getEvntType().getName() + "-" + getEntity() + "-" + getType();
	}

}
