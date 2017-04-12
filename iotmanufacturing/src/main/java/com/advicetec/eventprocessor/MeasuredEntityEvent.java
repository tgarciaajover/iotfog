package com.advicetec.eventprocessor;

import java.util.List;

import com.advicetec.core.AttributeValue;
import com.advicetec.measuredentitity.MeasuredEntity;

public class MeasuredEntityEvent extends Event 
{

	private String behaviorTransformation;
	private MeasuredEntity entity;
	private List<AttributeValue> parameters; 
	
	public MeasuredEntityEvent(String behavior, MeasuredEntity entity, List<AttributeValue> parameters) 
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

	public List<AttributeValue> getParameters() {
		return parameters;
	}
	
}
