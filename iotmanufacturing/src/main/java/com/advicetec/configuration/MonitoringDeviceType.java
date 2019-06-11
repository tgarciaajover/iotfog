package com.advicetec.configuration;

import java.util.HashMap;
import java.util.Map;

public enum MonitoringDeviceType {

	
	/**
	 * Not a valid Measuring device type.
	 */
	INVALID(0, "Invalid"),

	/**
	 * Mqtt measuring device  
	 */
	MQTT(1, "MQTT"),
		
	
	/**
	* Modbus measuring device   
	*/
	MODBUS(2, "MODBUS");
					
	/**
	* Unique code defining the measuring device type 
	*/
	public int code;
		
	/**
	* Name given to the measuring device type
	*/
	public String name;
		
	/**
	* map to search for measuring devices types by codes.
	*/
	private static final Map<Integer, MonitoringDeviceType> _map = new HashMap<Integer, MonitoringDeviceType>();

	/**
	* map to search for measuring devices type by name.
	*/
	private static final Map<String, MonitoringDeviceType> _map_name = new HashMap<String, MonitoringDeviceType>();

	
	static {
		for(MonitoringDeviceType type: MonitoringDeviceType.values()){
			_map.put(type.code, type);
		}

		for(MonitoringDeviceType type: MonitoringDeviceType.values()){
			_map_name.put(type.name, type);
		}

	}
		
	/**
	 * Constructor for the enumeration
	 * @param value  code for the measuring device type
	 * @param name   name for the measuring device type
	 */
	private MonitoringDeviceType(int value, String name) {
		this.code = value;
		this.name = name;
	}
		   
	/**
	 * Gets the code of the measuring device type

	 * @return measuring device type code
	*/
	public int getValue() {
		return this.code;
	}

	/**
	* Returns the measuring device type from its code
	* 
	* @param value  code of the measuring device type that needs to be returned
	* @return The measuring device type or null if not found in the map.
	*/
	public static MonitoringDeviceType from(int value){
		return _map.get(value);
	}

	/**
	* Returns the measuring device type from its name
	* 
	* @param name  name of the measuring device type that needs to be returned
	* @return The measuring device type or null if not found in the map.
	*/
	public static MonitoringDeviceType from_name(String name){
		return _map_name.get(name);
	}

	/**
	* Gets the name of the measuring device type.
	* @return
	*/
	public String getName(){
		return this.name;
	}

}
