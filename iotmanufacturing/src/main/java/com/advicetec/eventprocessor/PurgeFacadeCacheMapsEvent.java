package com.advicetec.eventprocessor;

import com.advicetec.measuredentitity.MeasuredEntityType;

public class PurgeFacadeCacheMapsEvent extends Event 
{


	/**
	 * Measuring entity for the aggregation.
	 */
	private Integer measuredEntity;
	
	/**
	 * Establishes the time of measured entity.
	 */
	MeasuredEntityType measuredEntityType;

	
	/**
	 * Constructor for the class.  
	 * 
	 * @param measuredEntity	measured entity identifier
	 * @param ownerType			measured entity type
	*/
	public PurgeFacadeCacheMapsEvent(Integer measuredEntity, MeasuredEntityType measuredEntityType) {
		
		super(EventType.PURGE_FACADE_MAPS, EventType.PURGE_FACADE_MAPS.getName() + Integer.toString(measuredEntity) + measuredEntityType.getName());
		
		this.measuredEntity = measuredEntity;
		this.measuredEntityType = measuredEntityType;
	}

	/**
	 * Gets the measured entity identifier  
	 * @return measured entity identifier
	 */
	public Integer getMeasuredEntity() {
		return measuredEntity;
	}

	/**
	 * Gets the measured entity type 
	 * @return measured entity type
	 */
	public MeasuredEntityType getOwnerType() {
		return measuredEntityType;
	}
	
	
}
