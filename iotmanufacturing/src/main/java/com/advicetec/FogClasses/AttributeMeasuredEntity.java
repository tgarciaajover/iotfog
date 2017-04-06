package com.advicetec.FogClasses;

import java.util.concurrent.TimeUnit; 

public class AttributeMeasuredEntity 
{
	 Attribute type; 
     int updateTime;
     TimeUnit timeunit;
     
     
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
