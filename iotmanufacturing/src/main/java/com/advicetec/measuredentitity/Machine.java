package com.advicetec.measuredentitity;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class Machine extends ProductionEntity {
	
	@JsonCreator
	public Machine(@JsonProperty("id") Integer id){
		super(id, MeasuredEntityType.MACHINE);
	}

}
