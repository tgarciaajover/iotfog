package com.advicetec.eventprocessor;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration for the different event aggregation types.  
 * 
 * @author andres
 *
 */
public enum AggregationEventType 
{

	/**
	 * Overall equipment aggregation 
	 */
	OEE(0, "OEE Aggregation"),
	/**
	 * Invalid event aggregation type
	 */
	INVALID(1, "Invalid");

	/**
	 * Unique code defining the aggregation event type 
	 */
	public int code;
	
	/**
	 * Name given to the event aggregation type.
	 */
	public String name;
	
	/**
	 * map to search for event types codes.
	 */
	private static final Map<Integer, AggregationEventType> _map = new HashMap<Integer, AggregationEventType>();
	
	static {
		for(AggregationEventType type: AggregationEventType.values()){
			_map.put(type.code, type);
		}
	}
	
	/**
	 * Constructor for the enumeration
	 * @param value  code for the aggregation event type
	 * @param name   name for the aggregation event type
	 */
	private AggregationEventType(int value, String name) {
	   this.code = value;
	   this.name = name;
	}
	   
	/**
	 * Gets the code of the aggregation event type
	 * @return
	 */
	public int getValue() {
	   return this.code;
	}

	/**
	 * Returns the event type aggregation from its code
	 * @param value  code of the event type aggregation that needs to be returned
	 * @return The aggregation event type or null if not found in the map.
	 */
	public static AggregationEventType from(int value){
		return _map.get(value);
	}
	
	
	/**
	 * Gets the name of the event aggregation type.
	 * @return
	 */
	public String getName(){
		return this.name;
	}
}
