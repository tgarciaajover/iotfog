package com.advicetec.eventprocessor;

import java.util.UUID;

public abstract class Event
{

	private EventType evntType;
	private String uuid;
	
	public Event(EventType type) 
	{
		super();
		this.evntType = type;
		this.uuid = UUID.randomUUID().toString();
	}

	public EventType getEvntType() {
		return evntType;
	}
	
	public String getKey(){
		return evntType.getName();
	}
	
	public String getId(){
		return uuid;
	}
}
