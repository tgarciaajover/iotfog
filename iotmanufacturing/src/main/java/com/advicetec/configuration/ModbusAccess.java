package com.advicetec.configuration;

import java.util.HashMap;
import java.util.Map;

public enum ModbusAccess {

	/**
	 * Not a valid modbus access.
	 */
	INVALID(0, "Invalid"),
	
	/**
	 * Read from the modbus slave
	 */
	READ(1, "Read"),
		
	/**
	* Write from the modbus slave. 
	*/
	WRITE(2, "Write"),
		
	/**
	* Read/Write from the modbus slave. 
	*/
	READ_WRITE(3, "Read_Write");
					
	/**
	* Unique code defining the modbus access
	*/
	public int code;
		
	/**
	* Name given to the modbus access.
	*/
	public String name;
		
	/**
	* map to search for modbus access by codes.
	*/
	private static final Map<Integer, ModbusAccess> _map = new HashMap<Integer, ModbusAccess>();
	
	/**
	* map to search for modbus access by name.
	*/
	private static final Map<String, ModbusAccess> _map_name = new HashMap<String, ModbusAccess>();
		
	static {
		for(ModbusAccess access: ModbusAccess.values()){
			_map.put(access.code, access);
		}

		for(ModbusAccess access: ModbusAccess.values()){
			_map_name.put(access.name, access);
		}
	}
		
	/**
	 * Constructor for the enumeration
	 * @param value  code for the modbus access
	 * @param name   name for the modbus access
	 */
	private ModbusAccess(int value, String name) {
		this.code = value;
		this.name = name;
	}
		   
	/**
	 * Gets the code of the modbus access

	 * @return modbus access code
	*/
	public int getValue() {
		return this.code;
	}

	/**
	* Returns the modbus access from its code
	* @param value  code of the modbus access that needs to be returned
	* @return The modbus access or null if not found in the map.
	*/
	public static ModbusAccess from(int value){
		return _map.get(value);
	}

	/**
	* Returns the modbus access from its name
	* @param name  name of the modbus access that needs to be returned
	* @return The modbus access or null if not found in the map.
	*/
	public static ModbusAccess from_name(String name){
		return _map_name.get(name);
	}

	/**
	* Gets the name of the modbus access.
	* @return
	*/
	public String getName(){
		return this.name;
	}

}
