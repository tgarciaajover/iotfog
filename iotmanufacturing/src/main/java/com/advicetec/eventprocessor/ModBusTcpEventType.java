package com.advicetec.eventprocessor;

import java.util.HashMap;
import java.util.Map;

public enum ModBusTcpEventType 
{

	READ_DISCRETE(0, "Read Discrete"),
	READ_REGISTER(1, "Read register"),
	WRITE_DISCRETE(2, "Write discrete"),
	WRITE_REGISTER(3, "Write register"),
	INVALID(4, "Invalid");

	public int code;
	public String name;
	
	private static final Map<Integer, ModBusTcpEventType> _map = new HashMap<Integer, ModBusTcpEventType>();
	
	static {
		for(ModBusTcpEventType type: ModBusTcpEventType.values()){
			_map.put(type.code, type);
		}
	}
	
	private ModBusTcpEventType(int value, String name) {
	   this.code = value;
	   this.name = name;
	}
	   
	public int getValue() {
	   return this.code;
	}

	public static ModBusTcpEventType from(int value){
		return _map.get(value);
	}
	
	public boolean equals(ModBusTcpEventType other){
		return this.code == other.code;
	}
}
