package com.advicetec.eventprocessor;

import java.util.UUID;

import com.advicetec.measuredentitity.MeasuredEntityType;

/**
 * Abstract class for all events. 
 * 
 * An event is any action or process that have to be processed taking 
 * information registered in the system.
 * Example of events are:
 * 		execute a behavior
 * 		aggregate information 
 * 		Show messages in displays
 * 		To take a sensor reading 
 * 
 * @author Andres Marentes
 *
 */
public abstract class Event
{

	/**
	 * Event type 
	 */
	private EventType evntType;
	
	/**
	 * Unique identifier of the event.
	 */
	private String uuid;
	
	/**
	 * Number of milliseconds to be repeated
	 */
	private long milliseconds; 
	
	/**
	 * It has to be repeated after processed or not.
	 */
	private boolean repeated;
	
	/**
	 * Key to identify the event within a queue. 
	 */
	private String key;
	
	/**
	 * Constructor for the event 
	 * @param type type of event being built
	 * 
	 *  By default it gives the event an unique identifier.
	 */
	public Event(EventType type, String eventKey) 
	{
		super();
		this.evntType = type;
		this.key = eventKey;
		this.uuid = UUID.randomUUID().toString();
		this.milliseconds = 0;
		this.repeated = false;
	}

	/**
	 * Gets the event type
	 * @return event type
	 */
	public EventType getEvntType() {
		return evntType;
	}
	
	/**
	 * Gets the unique key. This key is operational and different from the uuid. 
	 * 
	 * The key is used to determine if an event can be considered with the same purpose 
	 * 		as another already inserted in a queue. So we can skip events that will do the same job. 
	 * 
	 * @return key for the event.
	 */
	public String getKey(){
		return key;
	}
	
	/**
	 * Gets the unique event identifier
	 * 
	 * @return  event identifier
	 */
	public String getId(){
		return uuid;
	}

	/**
	 * Gets the time in milliseconds when the event should be reschedule. 
	 * 
	 * @return time in milliseconds for rescheduling
	 */
	public long getMilliseconds() {
		return milliseconds;
	}

	/**
	 * Sets the time in milliseconds when the event should be reschedule.
	 * 
	 * @param milliseconds milliseconds to reschedule
	 */
	public void setMilliseconds(long milliseconds) {
		this.milliseconds = milliseconds;
	}

	/**
	 * Gets whether or not the event should be repeated.
	 * @return true should be repeated, false otherwise.
	 */
	public boolean isRepeated() {
		return repeated;
	}

	/**
	 * Sets whether or not the event should be repeated.
	 *  
	 * @param repeated  true should be repeated, false otherwise.
	 */
	public void setRepeated(boolean repeated) {
		this.repeated = repeated;
	}

	/**
	 * This method should be overriden by all events that are related to a measured entity object. 
	 * It is used to delete the event from delayed queues. 
	 * @return. This base class returns -1 (an invalid measured entity id.) 
	 */
	public abstract Integer getEntity();
	
	public abstract MeasuredEntityType getOwnerType();
	
	/**
	 * Equals method, compare this event with the one given as parameter
	 * 
	 * We say that two events are equal if they have the same uuid.
	 *  
	 * @param o  event for comparison
	 * 
	 * @return true if equal, false otherwise.  
	 */
	public boolean equals(Object o){
		if (o instanceof Event) {
			
			Event other = (Event) o;
			return this.uuid.equals(other.uuid);
			
		} else {
			return false;
		}
		
	}
}
