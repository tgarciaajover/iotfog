package com.advicetec.MessageProcessor;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Delayed;

import com.advicetec.eventprocessor.Event;

public class DelayEvent implements Delayed
{

	/**
	 * Event to process.
	 */
	protected Event data;
	
	/**
	 *  This number is in milliseconds.
	 */
	protected long startTime;
	
	 
	public DelayEvent(Event data, long delay) {
		this.data = data;
		this.startTime = System.currentTimeMillis() + delay;
		
	}
	 
	@Override
	public long getDelay(TimeUnit unit) {
		long diff = this.startTime - System.currentTimeMillis();		
		return unit.convert(diff, TimeUnit.MILLISECONDS);
		
	}
	
	public Event getEvent(){
		return data;
	}
	
	@Override
	public int compareTo(Delayed o) {
		if (this.startTime < ((DelayEvent) o).startTime) {
			return -1;
	    }
	    if (this.startTime > ((DelayEvent) o).startTime) {
	        return 1;
	    }
	    return 0;
	}

	@Override
	public String toString() {
		return "{" +
	                "data='" + data + '\'' +
	                ", startTime=" + startTime +
	                '}';
	}
	
	public boolean equals(DelayEvent o){
		return (this.startTime == o.startTime) && this.getEvent().equals(o.getEvent()); 
	}
	
	public long getStartTime(){
		return startTime;
	}
	
	public String getId(){
		return this.data.getId();
	}
	
	public String getKey(){
		return this.data.getKey();
	}
}
