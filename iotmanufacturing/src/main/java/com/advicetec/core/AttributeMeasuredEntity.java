package com.advicetec.core;

import java.util.concurrent.TimeUnit; 

/**
 * Relates an attribute and when it changes its value.
 * If the attribute does not update its value, it is a constant, updateTime is -1.
 * IF the attribute value is updated by demand, updateTime is 0.
 * 
 * @author user
 *
 */
public class AttributeMeasuredEntity 
{
	 private Attribute type; 
     private int updateTime;
     private TimeUnit timeunit;
     
     
	 public AttributeMeasuredEntity( Attribute type, int updateTime,
			TimeUnit timeunit) {
		super();
		
		this.type = type;
		this.updateTime = updateTime;
		this.timeunit = timeunit;
	}


	public Attribute getType() {
		return type;
	}


	public int getUpdateTime() {
		return updateTime;
	}


	public TimeUnit getTimeunit() {
		return timeunit;
	}
      
}
