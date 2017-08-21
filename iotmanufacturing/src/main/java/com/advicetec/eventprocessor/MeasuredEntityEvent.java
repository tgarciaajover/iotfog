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
	private Integer entity;
	
	// Device where the measure was read
	private Integer device;
	
	// Port where the measure was read
	private Integer port;
	
	// List of attributes given to the event.
	private List<InterpretedSignal> parameters; 
		
	public MeasuredEntityEvent(String behavior, Integer entity, Integer device, Integer port, List<InterpretedSignal> parameters) 
	{
		super(EventType.MEASURING_ENTITY_EVENT);
		this.behaviorTransformation = behavior;
		this.entity = entity;
		this.device = device;
		this.port = port;
		this.parameters = parameters;
	}

	public Integer getDevice() {
		return device;
	}

	public Integer getPort() {
		return port;
	}

	public String getBehaviorTransformation() {
		return behaviorTransformation;
	}

	public Integer getMeasuredEntity() {
		return entity;
	}

	public List<InterpretedSignal> getParameters() {
		return parameters;
	}
	
	@Override
	public String toString() {
		return "entity=" + entity + "-" + "device=" + this.device + "-" + "Port=" + this.port + "-" + "behavior=" + behaviorTransformation;
	}

	public String getKey() {
		return getEvntType().getName() + "-" + this.device + "-" + this.port + "-" + behaviorTransformation;
	}

}
