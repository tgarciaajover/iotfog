package com.advicetec.utils;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class PredefinedPeriod 
{
    PredefinedPeriodType type;
    Calendar calendarFrom;
    Calendar calendarTo;
     
	public PredefinedPeriod(int year) {
		super();
		this.type = PredefinedPeriodType.YEAR;
		calendarFrom = new GregorianCalendar(year,1,1);
		calendarTo = null;
	}
	
	public PredefinedPeriod(int year, int month) {
		super();
		this.type = PredefinedPeriodType.MONTH;
		month = month - 1; // January is equals to 0
		int day = 1;
		calendarFrom = new GregorianCalendar(year,month,day);
		calendarTo = null;
	} 
	
	public PredefinedPeriod(int year, int month, int day) {
		super();
		this.type = PredefinedPeriodType.DAY;
		month = month - 1; // January is equals to 0
		calendarFrom = new GregorianCalendar(year, month, day);
		calendarTo = null;
	}

	public PredefinedPeriod(int year, int month, int day, int hour) {
		super();
		this.type = PredefinedPeriodType.HOUR;
		month = month - 1; // January is equals to 0
		int minute = 1;
		calendarFrom = new GregorianCalendar(year, month, day, hour, minute);
		calendarTo = null;
	}
	
	public PredefinedPeriod (LocalDateTime from, LocalDateTime to) {
		this.type = PredefinedPeriodType.INT_LT_HOUR;
		calendarFrom = new GregorianCalendar( from.getYear(), 
												from.getMonthValue()-1, 
												  from.getDayOfMonth(), 
												    from.getHour(), 
												      from.getMinute(), 0);
		
		calendarTo = new GregorianCalendar( to.getYear(), 
											  to.getMonthValue()-1, 
											  	to.getDayOfMonth(), 
											  	  to.getHour(), 
											  	  	to.getMinute(), 0);		
	}
	
	public PredefinedPeriodType getType() {
		return type;
	}
    
	public Calendar getCalendar() {
		return this.calendarFrom;
	}
	
	public Calendar getCalendarTo() {
		return this.calendarTo;
	}
	
	public String getKey() {
		
		String ret = "";
		switch (this.type)
		{
			case YEAR:
				ret = Integer.toString(this.calendarFrom.get(Calendar.YEAR));
				break;
			
			case MONTH:
				ret = Integer.toString(this.calendarFrom.get(Calendar.YEAR)) + 
					   "-" + Integer.toString(this.calendarFrom.get(Calendar.MONTH) + 1);
				break;
			
			case DAY:
				ret = Integer.toString(this.calendarFrom.get(Calendar.YEAR)) + 
						"-" + Integer.toString(this.calendarFrom.get(Calendar.MONTH) + 1) +
						  "-" + Integer.toString(this.calendarFrom.get(Calendar.DAY_OF_MONTH));
				break;
			case HOUR:
				ret = Integer.toString(this.calendarFrom.get(Calendar.YEAR)) + 
						"-" + Integer.toString(this.calendarFrom.get(Calendar.MONTH) + 1 ) +
							"-" + Integer.toString(this.calendarFrom.get(Calendar.DAY_OF_MONTH)) +
								"-" + Integer.toString(this.calendarFrom.get(Calendar.HOUR_OF_DAY)); 
				break;
			
			case INT_LT_HOUR:
				ret = "From-" + Integer.toString(this.calendarFrom.get(Calendar.YEAR)) + 
						"-" + Integer.toString(this.calendarFrom.get(Calendar.MONTH) + 1 ) +
						"-" + Integer.toString(this.calendarFrom.get(Calendar.DAY_OF_MONTH)) +
							"-" + Integer.toString(this.calendarFrom.get(Calendar.HOUR_OF_DAY)) + 
							  "-" + Integer.toString(this.calendarFrom.get(Calendar.MINUTE)) + 
					   "To-" +
							  Integer.toString(this.calendarTo.get(Calendar.YEAR)) + 
								"-" + Integer.toString(this.calendarTo.get(Calendar.MONTH) + 1 ) +
								"-" + Integer.toString(this.calendarTo.get(Calendar.DAY_OF_MONTH)) +
									"-" + Integer.toString(this.calendarTo.get(Calendar.HOUR_OF_DAY)) +
									  "-" + Integer.toString(this.calendarTo.get(Calendar.MINUTE));
				break;
			
			default:
				break;
		}
		
		return ret; 
				
	}
	
}
