package com.advicetec.eventprocessor;

import java.util.UUID;

public abstract class Event
{

	private EventType evntType;
	private String uuid;
	
	// Milliseconds when it has to be repeated
	private long milliseconds; 
	
	// it says if it has to be repeated or not.
	private boolean repeated;
		
	public Event(EventType type) 
	{
		super();
		this.evntType = type;
		this.uuid = UUID.randomUUID().toString();
		this.milliseconds = 0;
		this.repeated = false;
		
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

	public long getMilliseconds() {
		return milliseconds;
	}

	public void setMilliseconds(long milliseconds) {
		this.milliseconds = milliseconds;
	}

	public boolean isRepeated() {
		return repeated;
	}

	public void setRepeated(boolean repeated) {
		this.repeated = repeated;
	}

	/**
	 * This method should be overriden by all events that are related to a measured entity object. 
	 * It is used to delete the event from delayed queues. 
	 * @return. This base class returns -1 (an invalid measured entity id.) 
	 */
	public Integer getMeasuredEntity()
	{
		return new Integer(-1);
	}
	
	public boolean equals(Event o){
		return this.uuid.equals(o.uuid);
	}
}
