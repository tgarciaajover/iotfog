package com.advicetec.measuredentitity;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class Plant extends ProductionEntity {
	
	@JsonCreator
	public Plant(@JsonProperty("id") Integer id){
		super(id, MeasuredEntityType.PLANT);
	}

}
