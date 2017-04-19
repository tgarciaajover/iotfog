package com.advicetec.eventprocessor;

import java.util.List;

import com.advicetec.core.AttributeValue;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

public class MeasuredEntityEvent extends Event 
{

	// name of the behavior transformation
	private String behaviorTransformation;
	
	// Entity involved for this event.
	private String entity;
	
	// List of attributes given to the event.
	private List<InterpretedSignal> parameters; 
	
	public MeasuredEntityEvent(String behavior, String entity, List<InterpretedSignal> parameters) 
	{
		super(EventType.MEASURING_ENTITY_EVENT);
		this.behaviorTransformation = behavior;
		this.entity = entity;
		this.parameters = parameters;
	}

	public String getBehaviorTransformation() {
		return behaviorTransformation;
	}

	public String getEntity() {
		return entity;
	}

	public List<InterpretedSignal> getParameters() {
		return parameters;
	}
	
	@Override
	public String toString() {
		return "{" +
	                "entity='" + entity + '\'' +
	                ", num_paramters=" + parameters.size() +
	                ", behavior=" + behaviorTransformation +
	                '}';
	}
}
