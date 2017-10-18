package com.advicetec.MessageProcessor;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.eventprocessor.Event;

/**
 * This class models events which will be processed in the future.
 * It contains an <code>Event</code> and the time (delay) when it will be 
 * processed.
 * <p>
 * This class implements the <code>Delayed</code> interface that marks objects
 * that will execute after a specified time.
 * 
 * @author advicetec
 * @see Event
 * @see Delayed
 * 
 */
public class DelayEvent implements Delayed
{

	static final Logger logger = LogManager.getLogger(DelayEvent.class.getName()); 
	/**
	 * Event to process.
	 */
	protected Event data;
	
	/**
	 *  Time in milliseconds when the event will be processed.
	 */
	protected long startTime;
	
	/**
	 * Constructor. 
	 * @param data event to be processed in the future.
	 * @param delay time in milliseconds when the event will be processed.
	 */
	public DelayEvent(Event data, long delay) {
		this.data = data;
		this.startTime = System.currentTimeMillis() + delay;
		
	}
	 
	@Override
	public long getDelay(TimeUnit unit) {
		long diff = this.startTime - System.currentTimeMillis();		
		return unit.convert(diff, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Returns the event associated to this DelayedEvent.
	 * @return the event related to this DelayedEvent.
	 */
	public Event getEvent(){
		return data;
	}
	
	@Override
	public int compareTo(Delayed o) {
		// if this is less than the parameter
		if (this.startTime < ((DelayEvent) o).startTime) {
			return -1;
	    }
		// if this is higher than the parameter
	    if (this.startTime > ((DelayEvent) o).startTime) {
	        return 1;
	    }
	    // if this and the parameter are equals.
	    return 0;
	}

	@Override
	public String toString() {
		return "{" +
	                "data='" + data + '\'' +
	                ", startTime=" + startTime +
	                '}';
	}
	/**
	 * Returns <code>TRUE</code> if the given Delayed Event and this object
	 * have the same event key, <code>FALSE</code> otherwise.
	 * @param o the object to compare this element.
	 * @return  <code>TRUE</code> if the given Delayed Event and this object
	 * have the same event key, <code>FALSE</code> otherwise.
	 */
	public boolean equals(Object o){
		if (o instanceof DelayEvent) {
			DelayEvent delay = (DelayEvent) o; 
			logger.debug("This.key:" + this.getKey() + "Other.key: " + delay.getKey() );
			return this.getKey().equals(delay.getKey());
		} else {
			return false;
		}
	}
	/**
	 * Returns the scheduled time, in milliseconds, for this event.
	 * @return the scheduled time, in milliseconds, for this event.
	 */
	public long getStartTime(){
		return startTime;
	}
	
	/**
	 * Returns the unique identifier of this event.
	 * @return unique identifier of this event.
	 * @see Event#getId()
	 */
	public String getId(){
		return this.data.getId();
	}
	
	/**
	 * Returns the operational relation of this event.
	 * @return the operational information of this event.
	 * @see Event#getKey()
	 */
	public String getKey(){
		return this.data.getKey();
	}
}
