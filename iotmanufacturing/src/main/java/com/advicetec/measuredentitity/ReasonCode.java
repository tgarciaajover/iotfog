package com.advicetec.measuredentitity;

public class ReasonCode 
{
	String id;
	String description;

	public ReasonCode(String id, String description) {
		super();
		this.id = id;
		this.description = description;
	}
	
	public String getId() {
		return id;
	}
		
	public String getDescription() {
		return description;
	}
	
	
}
