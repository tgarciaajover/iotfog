package com.advicetec.eventprocessor;

import java.util.List;

import com.advicetec.measuredentitity.MeasuredAttributeValue;
import com.advicetec.measuredentitity.MeasuredEntity;

public class MeasuredEntityEvent extends Event 
{

	private String behaviorTransformation;
	MeasuredEntity entity;
	List<MeasuredAttributeValue> parameters; 
	
	public MeasuredEntityEvent(String behavior, MeasuredEntity entity, List<MeasuredAttributeValue> parameters) 
	{
		super(EventType.MEASURING_ENTITY_EVENT);
		this.behaviorTransformation = behavior;
		this.entity = entity;
		this.parameters = parameters;
	}

	public String getBehaviorTransformation() {
		return behaviorTransformation;
	}

	public MeasuredEntity getEntity() {
		return entity;
	}

	public List<MeasuredAttributeValue> getParameters() {
		return parameters;
	}
	
}
