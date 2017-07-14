package com.advicetec.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class PredefinedPeriod 
{
    PredefinedPeriodType type;
    Calendar calendar;
     
	public PredefinedPeriod(int year) {
		super();
		this.type = PredefinedPeriodType.YEAR;
		calendar = new GregorianCalendar(year,0,0);
	}
	
	public PredefinedPeriod(int year, int month) {
		super();
		this.type = PredefinedPeriodType.MONTH;
		month = month - 1; // January is equals to 0
		int day = 0;
		calendar = new GregorianCalendar(year,month,day);
	} 
	
	public PredefinedPeriod(int year, int month, int day) {
		super();
		this.type = PredefinedPeriodType.DAY;
		month = month - 1; // January is equals to 0
		calendar = new GregorianCalendar(year, month, day);
	}

	public PredefinedPeriod(int year, int month, int day, int hour) {
		super();
		this.type = PredefinedPeriodType.HOUR;
		month = month - 1; // January is equals to 0
		int second = 1;
		calendar = new GregorianCalendar(year, month, day, hour, second);
	}
	
	public PredefinedPeriodType getType() {
		return type;
	}
    
	public Calendar getCalendar() {
		return this.calendar;
	}
	
	public String getKey() {
		
		String ret = "";
		switch (this.type)
		{
			case YEAR:
				ret = Integer.toString(this.calendar.get(Calendar.YEAR));
				break;
			
			case MONTH:
				ret = Integer.toString(this.calendar.get(Calendar.YEAR)) + 
					   "-" + Integer.toString(this.calendar.get(Calendar.MONTH) + 1);
				break;
			
			case DAY:
				ret = Integer.toString(this.calendar.get(Calendar.YEAR)) + 
						"-" + Integer.toString(this.calendar.get(Calendar.MONTH) + 1) +
						  "-" + Integer.toString(this.calendar.get(Calendar.DAY_OF_MONTH));
				break;
			case HOUR:
				ret = Integer.toString(this.calendar.get(Calendar.YEAR)) + 
						"-" + Integer.toString(this.calendar.get(Calendar.MONTH) + 1 ) +
							"-" + Integer.toString(this.calendar.get(Calendar.DAY_OF_MONTH)) +
								"-" + Integer.toString(this.calendar.get(Calendar.HOUR_OF_DAY)); 
				break;
			
			default:
				break;
		}
		
		return ret; 
				
	}
	
}
