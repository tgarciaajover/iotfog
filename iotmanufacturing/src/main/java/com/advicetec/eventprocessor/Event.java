package com.advicetec.eventprocessor;

public abstract class Event
{

	EventType evntType;
	
	public Event(EventType type) 
	{
		super();
		this.evntType = type;
	}

	public EventType getEvntType() {
		return evntType;
	}
	
	
	
}
