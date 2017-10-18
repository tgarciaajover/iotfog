package com.advicetec.eventprocessor;

import java.util.HashMap;
import java.util.Map;

public enum ModBusTcpEventType 
{

	/**
	 * Read a discrete (digital input) from the modbus slave. Modbus Function FC02 
	 */
	READ_DISCRETE(0, "Read Discrete"),
	
	/**
	 * Read a input register from the modbus slave. Modbus Function FC04 
	 */
	READ_REGISTER(1, "Read register"),
	
	/**
	 * Read a holding register from the modbus slave.  Modbus Function FC03.
	 */
	READ_HOLDING_REGISTER(2, "Read Holding register"),
	
	/**
	 * Write in digital output. Modbus Function FC05.
	 */
	WRITE_DISCRETE(3, "Write discrete"),
	
	/**
	 * Write in a holding register.  
	 */
	WRITE_REGISTER(4, "Write register"),
	
	/**
	 * Not a valid modbus event.
	 */
	INVALID(5, "Invalid");

	/**
	 * Unique code defining the modbus event type 
	 */
	public int code;
	
	/**
	 * Name given to the modbus event type.
	 */
	public String name;
	
	/**
	 * map to search for modbus event types by codes.
	 */
	private static final Map<Integer, ModBusTcpEventType> _map = new HashMap<Integer, ModBusTcpEventType>();
	
	static {
		for(ModBusTcpEventType type: ModBusTcpEventType.values()){
			_map.put(type.code, type);
		}
	}
	
	/**
	 * Constructor for the enumeration
	 * @param value  code for the modbus event type
	 * @param name   name for the modbus event type
	 */
	private ModBusTcpEventType(int value, String name) {
	   this.code = value;
	   this.name = name;
	}
	   
	/**
	 * Gets the code of the modbus event type

	 * @return modbus event type code
	 */
	public int getValue() {
	   return this.code;
	}

	/**
	 * Returns the modbus event type from its code
	 * @param value  code of the modbus event type that needs to be returned
	 * @return The modbus event type or null if not found in the map.
	 */
	public static ModBusTcpEventType from(int value){
		return _map.get(value);
	}
	
	/**
	 * Gets the name of the modbus event type.
	 * @return
	 */
	public String getName(){
		return this.name;
	}
}
