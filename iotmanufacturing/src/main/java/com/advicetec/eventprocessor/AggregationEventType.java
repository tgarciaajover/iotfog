package com.advicetec.eventprocessor;

import java.util.HashMap;
import java.util.Map;

public enum AggregationEventType 
{

	OEE(0, "OEE Aggregation"),
	INVALID(1, "Invalid");

	public int code;
	public String name;
	
	private static final Map<Integer, AggregationEventType> _map = new HashMap<Integer, AggregationEventType>();
	
	static {
		for(AggregationEventType type: AggregationEventType.values()){
			_map.put(type.code, type);
		}
	}
	
	private AggregationEventType(int value, String name) {
	   this.code = value;
	   this.name = name;
	}
	   
	public int getValue() {
	   return this.code;
	}

	public static AggregationEventType from(int value){
		return _map.get(value);
	}
	
	public boolean equals(AggregationEventType other){
		return this.code == other.code;
	}
	
	public String getName(){
		return this.name;
	}
}
