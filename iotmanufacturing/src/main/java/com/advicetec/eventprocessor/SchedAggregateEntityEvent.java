package com.advicetec.eventprocessor;

import java.util.List;

import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

public class SchedAggregateEntityEvent extends Event 
{
	
	/**
	 * name of the behavior name
	 */
	private String aggregateMethod;
	
	/**
	 * Entity involved for this event.
	 */
	private Integer entity;
	
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
	 * @param aggregateMethod		aggregateMethod name (program) to execute
	 * @param entity		measured entity giving the context for the program. 
	 * @param device		measuring device where the information was registered and that triggers this behavior
	 * @param port			port in the measuring device where the information was registered and that triggers this behavior
	 * @param parameters	List of interpreted signals result of the transformation.
	 */
	public SchedAggregateEntityEvent(String aggregateMethod, Integer entity, Integer device, Integer port, List<InterpretedSignal> parameters) 
	{
		super(EventType.SCHED_AGGREGATION_EVENT, 
					EventType.SCHED_AGGREGATION_EVENT.getName() + "-" + 
						Integer.toString(entity) + "-" + aggregateMethod );
		
		this.aggregateMethod = aggregateMethod;
		this.entity = entity;
		this.device = device;
		this.port = port;
		this.parameters = parameters;
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
	public String getAggregateMethod() {
		return aggregateMethod;
	}

	/**
	 * Gets the measured entity acting as context for the behavior program
	 * 
	 * @return measured entity identifier
	 */
	public Integer getMeasuredEntity() {
		return entity;
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
		return "entity=" + entity + "-" + "device=" + this.device + "-" + "Port=" + this.port + "-" + "aggregateMethod=" + aggregateMethod;
	}

	@Override
	public Integer getEntity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MeasuredEntityType getOwnerType() {
		// TODO Auto-generated method stub
		return null;
	}
}
