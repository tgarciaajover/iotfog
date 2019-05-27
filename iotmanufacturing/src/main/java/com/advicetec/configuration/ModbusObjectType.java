package com.advicetec.configuration;

import java.util.HashMap;
import java.util.Map;

public enum ModbusObjectType {

	/**
	 * Read a coil (digital input) from the modbus slave. Modbus Function FC02 
	 */
	COIL(0, "Coil"),
		
	
	/**
	* Read a discrete (digital input) from the modbus slave. Modbus Function FC02 
	*/
	DISCRETE(1, "Discrete"),
		
	/**
	* Read a input register from the modbus slave. Modbus Function FC04 
	*/
	REGISTER(2, "register"),
		
	/**
	 * Read a holding register from the modbus slave.  Modbus Function FC03.
	*/
	HOLDING_REGISTER(3, "Holding register"),
	
	/**
	 * Not a valid modbus object type.
	 */
	INVALID(4, "Invalid");
		
	/**
	* Unique code defining the modbus object type 
	*/
	public int code;
		
	/**
	* Name given to the modbus object type.
	*/
	public String name;
		
	/**
	* map to search for modbus object types by codes.
	*/
	private static final Map<Integer, ModbusObjectType> _map = new HashMap<Integer, ModbusObjectType>();
		
	static {
		for(ModbusObjectType type: ModbusObjectType.values()){
			_map.put(type.code, type);
		}
	}
		
	/**
	 * Constructor for the enumeration
	 * @param value  code for the modbus object type
	 * @param name   name for the modbus object type
	 */
	private ModbusObjectType(int value, String name) {
		this.code = value;
		this.name = name;
	}
		   
	/**
	 * Gets the code of the modbus object type

	 * @return modbus object type code
	*/
	public int getValue() {
		return this.code;
	}

	/**
	* Returns the modbus object type from its code
	* @param value  code of the modbus object type that needs to be returned
	* @return The modbus object type or null if not found in the map.
	*/
	public static ModbusObjectType from(int value){
		return _map.get(value);
	}
		
	/**
	* Gets the name of the modbus object type.
	* @return
	*/
	public String getName(){
		return this.name;
	}

}
