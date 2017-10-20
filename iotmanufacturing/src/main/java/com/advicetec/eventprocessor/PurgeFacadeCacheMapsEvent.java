package com.advicetec.eventprocessor;

import com.advicetec.measuredentitity.MeasuredEntityType;

public class PurgeFacadeCacheMapsEvent extends Event 
{


	/**
	 * Measuring entity for the aggregation.
	 */
	private Integer entity;
	
	/**
	 * Establishes the time of measured entity.
	 */
	MeasuredEntityType entityType;

	
	/**
	 * Constructor for the class.  
	 * 
	 * @param measuredEntity	measured entity identifier
	 * @param ownerType			measured entity type
	*/
	public PurgeFacadeCacheMapsEvent(Integer entity, MeasuredEntityType entityType) {
		
		super(EventType.PURGE_FACADE_MAPS, EventType.PURGE_FACADE_MAPS.getName() + Integer.toString(entity) + entityType.getName());
		
		this.entity = entity;
		this.entityType = entityType;
	}

	/**
	 * Gets the measured entity identifier  
	 * @return measured entity identifier
	 */
	public Integer getEntity() {
		return entity;
	}

	/**
	 * Gets the measured entity type 
	 * @return measured entity type
	 */
	public MeasuredEntityType getOwnerType() {
		return entityType;
	}
	
	
}
