package com.advicetec.eventprocessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Enumeration with the event types
 * 
 * @author Andres Marentes
 *
 */
public enum EventType 
{

	/**
	 * It is created to establish a timer in the system. for now it is not used.  
	 */
	TIMER_EVENT(0,"Timer_Event"),
	
	/**
	 * The measuring entity event is used to execute a behavior (measured entity behavior or state behavior).
	 */
	MEASURING_ENTITY_EVENT(1,"Measuring_Entity_Event"),
	
	/**
	 * The meta model event is designed to register a change in the meta model. For now it is not used, because this changes occurs in the rest interface.
	 */
	META_MODEL_EVENT(2,"Meta_Model_Event"),
	
	/**
	 *  The production order event is designed to register the arrival of a new production order. For now it is not used, because this changes occurs in the rest interface.
	 */
	PRODUCTION_ORDER(3,"Production_Order"),
	
	/**
	 * The web application query is designed to respond to queryes from the web application. For now it is not used, because this changes occurs in the rest interface.
	 */
	WEB_APPLICATION_QUERY_EVENT(4,"Web_Application_Query_Event"),
	
	/**
	 * The Display event is designed to show messages in a display.
	 */
	DISPLAY_EVENT(5,"Display_Event"),
	
	/**
	 * The modbus read event is designed to read from a sensor using the modbus protocol.
	 */
	MODBUS_READ_EVENT(6,"Modbus_Read_Event"),
	
	/**
	 * The modbus write event is designed to write to sensors using the modbus protocol.
	 */
	MODBUS_WRITE_EVENT(7,"Modbus_Write_Event"),
	
	/**
	 * The Aggregation event is designed to execute an aggregation in data
	 */
	AGGREGATION_EVENT(8,"Aggregation_Event"),
	
	/**
	 * Event to test handlers
	 */
	TEST(9,"Test"),
	
	/**
	 * Event to purge maps controlling caches. 
	 */
	PURGE_FACADE_MAPS(10, "Purge_Facade_Maps"),
	
	/**
	 * The Sched Aggregation event is designed to execute an aggregation in data
	 */
	SCHED_AGGREGATION_EVENT(12,"Sched_Aggregation_Event"),
	
	/**
	 * Invalid event. 
	 */
	INVALID(11,"Invalid_Event");

	/**
	 * Unique code defining the event type 
	 */
	private int code;
	
	/**
	 * Name given to the event type.
	 */
	private String name;

	/**
	 * map to search for modbus event types by codes.
	 */
	private static final Map<Integer, EventType> _map = new HashMap<Integer, EventType>();
	
	static {
		for(EventType type: EventType.values()){
			_map.put(type.code, type);
		}
	}

	
	/**
	 * Constructor for the enumeration
	 * @param value  code for the event type
	 * @param name   name for the event type
	 */
	private EventType(int value, String name) {
		this.code = value;
		this.name = name;
	}

	/**
	 * Gets the code of the event type
	 * @return event code
	 */
	public int getValue() {
		return this.code;
	}

	/**
	 * Gets the name of the event type.
	 * @return event type name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Returns the set of event types created in the system

	 * @return The event types set.
	 */
	public static Set<Entry<Integer, EventType>> getList(){
		return _map.entrySet();
	}
	
}
