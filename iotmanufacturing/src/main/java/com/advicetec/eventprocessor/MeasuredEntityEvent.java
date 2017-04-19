package com.advicetec.eventprocessor;

import java.util.List;

import com.advicetec.core.AttributeValue;
import com.advicetec.measuredentitity.MeasuredEntity;

public class MeasuredEntityEvent extends Event 
{

	// name of the behavior transformation
	private String behaviorTransformation;
	
	// Entity involved for this event.
	private MeasuredEntity entity;
	
	// List of attributes given to the event.
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
	
	@Override
	public String toString() {
		return "{" +
	                "entity='" + entity.getId() + '\'' +
	                ", num_paramters=" + parameters.size() +
	                ", behavior=" + behaviorTransformation +
	                '}';
	}
}
