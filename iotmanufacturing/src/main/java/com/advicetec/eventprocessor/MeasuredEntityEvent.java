package com.advicetec.eventprocessor;

import java.util.List;

import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

public class MeasuredEntityEvent extends Event 
{
	
	/**
	 * name of the behavior name
	 */
	private String behaviorName;
	
	/**
	 * Entity involved for this event.
	 */
	private Integer entity;
	
	/***
	 * Entity type
	 */
	
	private MeasuredEntityType entityType;
	
	/**
	 * Device where the measure was read
	 */
	private Integer device;
	
	/**
	 * Port where the measure was read 
	 */
	private Integer port;
	
	/**
	 * List of attributes given to the event.
	 */
	private List<InterpretedSignal> parameters; 
		
	/**
	 * Constructor for the class 
	 * 
	 * @param behavior		behavior name (program) to execute
	 * @param entity		measured entity giving the context for the program. 
	 * @param device		measuring device where the information was registered and that triggers this behavior
	 * @param port			port in the measuring device where the information was registered and that triggers this behavior
	 * @param parameters	List of interpreted signals result of the transformation.
	 */
	public MeasuredEntityEvent(String behavior, Integer entity, MeasuredEntityType entityType, Integer device, Integer port, List<InterpretedSignal> parameters) 
	{
		super(EventType.MEASURING_ENTITY_EVENT, 
					EventType.MEASURING_ENTITY_EVENT.getName() + "-" + 
						Integer.toString(entity) + "-" + behavior );
		
		this.behaviorName = behavior;
		this.entity = entity;
		this.device = device;
		this.port = port;
		this.parameters = parameters;
		this.entityType = entityType;
	}

	/**
	 * Gets the measuring device where the information was registered
	 * 
	 * @return measuring device identifier.
	 */
	public Integer getDevice() {
		return device;
	}

	/**
	 * Gets the measuring device port where the information was registered
	 * 
	 * @return  measuring device port identifier.
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * Gets the behavior program in string 
	 * 
	 * @return behavior program
	 */
	public String getBehaviorName() {
		return behaviorName;
	}
	
	/**
	 * Gets the list of parameters required to execute the behavior
	 * 
	 * @return Interpreted signal list.
	 */
	public List<InterpretedSignal> getParameters() {
		return parameters;
	}
	
	/**
	 * Serializes the object to string
	 * 
	 * @return String serialization of the event.
	 */
	@Override
	public String toString() {
		return "entity=" + entity + "-" + "entity type:" + entityType + "-" + "device=" + this.device + "-" + "Port=" + this.port + "-" + "behavior=" + behaviorName;
	}

	/**
	 * Gets the measured entity acting as context for the behavior program
	 * 
	 * @return measured entity identifier
	 */
	@Override
	public Integer getEntity() {
		return entity;
	}
	
	
	@Override
	public MeasuredEntityType getOwnerType() {
		return entityType;
	}
}
