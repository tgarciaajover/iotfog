package com.advicetec.utils;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.eventprocessor.OEEAggregationEventProcessor;

public class PredefinedPeriod 
{
    PredefinedPeriodType type;
    Calendar calendarFrom;
    Calendar calendarTo;
    
    static Logger logger = LogManager.getLogger(PredefinedPeriod.class.getName());
    
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
	
	
	public static PredefinedPeriod getInstanceFrom(String periodKey) {
		
		String[] tokens = periodKey.split("-");
		
		try{
			if(tokens.length == 1){ // it is only the year
				return new PredefinedPeriod(Integer.parseInt(tokens[0]));
			} else if(tokens.length == 2){ // it is the year and month 
				return new PredefinedPeriod(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
			} else if(tokens.length == 3){ // it is the year, month, day
				return new PredefinedPeriod(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]) );
			} else if (tokens.length == 4){ // it is the year, month, day, hour
				return new PredefinedPeriod(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]) );
			} else{
				Logger logger = LogManager.getLogger(PredefinedPeriod.class.getName());
				logger.error("error creating a predefined period from String key numParts:" + tokens.length + " key given:" + periodKey);
			}
		} catch (NumberFormatException e){
			logger.error("The period given has an invalid format value:" + periodKey);
		}
		return null;
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
				ret = String.format("%04d", this.calendarFrom.get(Calendar.YEAR));
				break;
			
			case MONTH:
				ret = String.format("%04d", this.calendarFrom.get(Calendar.YEAR)) + 
					   "-" + String.format("%02d", this.calendarFrom.get(Calendar.MONTH) + 1) ;
				break;
			
			case DAY:
				ret = String.format("%04d", this.calendarFrom.get(Calendar.YEAR)) + 
						"-" + String.format("%02d", this.calendarFrom.get(Calendar.MONTH) + 1) +
						  "-" + String.format("%02d", this.calendarFrom.get(Calendar.DAY_OF_MONTH));
				break;
			case HOUR:
				ret = String.format("%04d", this.calendarFrom.get(Calendar.YEAR)) + 
						"-" + String.format("%02d", this.calendarFrom.get(Calendar.MONTH) + 1 ) +
							"-" + String.format("%02d", this.calendarFrom.get(Calendar.DAY_OF_MONTH)) +
								"-" + String.format("%02d", this.calendarFrom.get(Calendar.HOUR_OF_DAY)); 
				break;
			
			case INT_LT_HOUR:
				ret = "From-" + String.format("%04d", this.calendarFrom.get(Calendar.YEAR)) + 
						"-" + String.format("%02d", this.calendarFrom.get(Calendar.MONTH) + 1 ) +
						"-" + String.format("%02d", this.calendarFrom.get(Calendar.DAY_OF_MONTH)) +
							"-" + String.format("%02d", this.calendarFrom.get(Calendar.HOUR_OF_DAY)) + 
							  "-" + String.format("%02d", this.calendarFrom.get(Calendar.MINUTE)) + 
					   "To-" +
					   String.format("%04d", this.calendarTo.get(Calendar.YEAR)) + 
								"-" + String.format("%02d", this.calendarTo.get(Calendar.MONTH) + 1 ) +
								"-" + String.format("%02d", this.calendarTo.get(Calendar.DAY_OF_MONTH)) +
									"-" + String.format("%02d", this.calendarTo.get(Calendar.HOUR_OF_DAY)) +
									  "-" + String.format("%02d", this.calendarTo.get(Calendar.MINUTE));
				break;
			
			default:
				break;
		}
		
		return ret; 
				
	}
	
	public LocalDateTime getLocalDateTime(){

		switch (this.type)
		{
			case YEAR:
				return LocalDateTime.of(this.calendarFrom.get(Calendar.YEAR), 1, 1, 0, 0);
			
			case MONTH:
				return LocalDateTime.of(this.calendarFrom.get(Calendar.YEAR), this.calendarFrom.get(Calendar.MONTH) + 1, 1, 0, 0);
			
			case DAY:
				return LocalDateTime.of(this.calendarFrom.get(Calendar.YEAR), 
										 this.calendarFrom.get(Calendar.MONTH) + 1, 
										  this.calendarFrom.get(Calendar.DAY_OF_MONTH), 0, 0);				
			case HOUR:
				return LocalDateTime.of(this.calendarFrom.get(Calendar.YEAR), 
						 this.calendarFrom.get(Calendar.MONTH) + 1, 
						  this.calendarFrom.get(Calendar.DAY_OF_MONTH), 
						  	this.calendarFrom.get(Calendar.HOUR_OF_DAY), 0);				
			
			case INT_LT_HOUR:
				return LocalDateTime.of(this.calendarFrom.get(Calendar.YEAR), 
						 this.calendarFrom.get(Calendar.MONTH) + 1, 
						  this.calendarFrom.get(Calendar.DAY_OF_MONTH), 
						  	this.calendarFrom.get(Calendar.HOUR_OF_DAY), 
						  	 this.calendarFrom.get(Calendar.MINUTE));				
			
			default:
				return null;
		}

	}
	
	public boolean equals(PredefinedPeriod another){
		return(getKey().equalsIgnoreCase(another.getKey()));
	}
	
}
