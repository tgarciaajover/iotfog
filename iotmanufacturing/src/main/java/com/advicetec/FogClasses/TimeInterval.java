package com.advicetec.FogClasses;

import java.time.LocalDateTime;

public class TimeInterval 
{
	private
		
		LocalDateTime startDateTime;
		LocalDateTime endDateTime;
		
	public TimeInterval(LocalDateTime startDateTime,
				LocalDateTime endDateTime) 
	{
		super();
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
	}

	public LocalDateTime getStartDateTime() 
	{
		return startDateTime;
	}

	public LocalDateTime getEndDateTime() 
	{
		return endDateTime;
	}

	
	public String toString()
	{
		return startDateTime.toString() + endDateTime.toString();
	}
}
