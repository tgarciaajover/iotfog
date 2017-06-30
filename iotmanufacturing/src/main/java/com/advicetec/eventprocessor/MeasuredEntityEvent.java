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
	
	// Milliseconds when it has to be repeated
	private long milliseconds; 
	
	// it says if it has to be repeated or not.
	private boolean repeated;
	
	
	public MeasuredEntityEvent(String behavior, Integer entity, Integer device, Integer port, List<InterpretedSignal> parameters) 
	{
		super(EventType.MEASURING_ENTITY_EVENT);
		this.behaviorTransformation = behavior;
		this.entity = entity;
		this.device = device;
		this.port = port;
		this.parameters = parameters;
		this.repeated = false;
		this.milliseconds = 0;
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

	public Integer getEntity() {
		return entity;
	}

	public List<InterpretedSignal> getParameters() {
		return parameters;
	}
	
	
	
	public long getMilliseconds() {
		return milliseconds;
	}

	public void setMilliseconds(long milliseconds) {
		this.milliseconds = milliseconds;
	}

	public boolean isRepeated() {
		return repeated;
	}

	public void setRepeated(boolean repeated) {
		this.repeated = repeated;
	}

	@Override
	public String toString() {
		return "entity=" + entity + "-" + "device=" + this.device + "-" + "Port=" + this.port + "-" + "behavior=" + behaviorTransformation;
	}

	public String getKey() {
		return getEvntType().getName() + "-" + this.device + "-" + this.port + "-" + behaviorTransformation;
	}

}
