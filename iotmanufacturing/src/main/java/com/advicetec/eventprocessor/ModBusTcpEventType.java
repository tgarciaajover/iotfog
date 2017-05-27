package com.advicetec.eventprocessor;

public enum ModBusTcpEventType 
{

	READ_DISCRETE(0,"Read Discrete"),
	READ_REGISTER(1,"Read Register"),
	WRITE_DISCRETE(2,"Write Discrete"),
	WRITE_REGISTER(3,"Write Register"),
	INVALID(4,"Invalid");

	private int code;
	private String name;
	   
	private ModBusTcpEventType(int value, String name) {
	   this.code = value;
	   this.name = name;
	}
	   
	public int getValue() {
	   return this.code;
	}
	   
	public String getName() {
	   return this.name;
	}

}
