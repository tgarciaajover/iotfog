package com.advicetec.core;

import java.time.LocalDateTime;
import java.util.List;

import com.advicetec.measuredentitity.MeasuringState;

public interface EntityFacade {

	AttributeValue getNewestByAttributeName(String attributeId);
	
	MeasuringState getCurrentState();
	
	List<AttributeValue> getByIntervalByAttributeName(String attributeId, LocalDateTime from, LocalDateTime to);
	
	AttributeValue getExecutedObjectAttribute(String attributeId); 
	
}
