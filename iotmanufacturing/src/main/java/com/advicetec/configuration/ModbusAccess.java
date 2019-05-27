package com.advicetec.configuration;

import java.util.HashMap;
import java.util.Map;

public enum ModbusAccess {

	/**
	 * Read from the modbus slave
	 */
	READ(0, "Read"),
		
	
	/**
	* Write from the modbus slave. 
	*/
	WRITE(1, "Write"),
		
	/**
	* Read/Write from the modbus slave. 
	*/
	READ_WRITE(2, "Read_Write"),
			
	/**
	 * Not a valid modbus access.
	 */
	INVALID(3, "Invalid");
		
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
		
	static {
		for(ModbusAccess access: ModbusAccess.values()){
			_map.put(access.code, access);
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
	* Gets the name of the modbus access.
	* @return
	*/
	public String getName(){
		return this.name;
	}

}
