package com.advicetec.utils;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class that models a predefined period between two dates.
 * The type of a predefined period ranges from years to hours or less_than_hour.
 * @see PredefinedPeriodType
 * @author advicetec
 *
 */
public class PredefinedPeriod 
{
	/**
	 * Defines a time unit.
	 * @see PredefinedPeriodType
	 */
    PredefinedPeriodType type;
    /**
     * initial time
     */
    Calendar calendarFrom;
    /**
     * final time
     */
    Calendar calendarTo;
    
    static Logger logger = LogManager.getLogger(PredefinedPeriod.class.getName());
    
    /**
	 * Constructs an open predefined period in hours from the given parameters.
	 * Final time will be null.
	 * 
	 * @param year Initial year.
	 */
	public PredefinedPeriod(int year) {
		super();
		this.type = PredefinedPeriodType.YEAR;
		calendarFrom = new GregorianCalendar(year,1,1);
		calendarTo = null;
	}
	
	/**
	 * Constructs an open predefined period in hours from the given parameters.
	 * Final time will be null.
	 * 
	 * @param year Initial year.
	 * @param month Initial month.
	 */
	public PredefinedPeriod(int year, int month) {
		super();
		this.type = PredefinedPeriodType.MONTH;
		month = month - 1; // January is equals to 0
		int day = 1;
		calendarFrom = new GregorianCalendar(year,month,day);
		calendarTo = null;
	} 
	
	/**
	 * Constructs an open predefined period in hours from the given parameters.
	 * Final time will be null.
	 * 
	 * @param year Initial year.
	 * @param month Initial month.
	 * @param day Initial day.
	 */
	public PredefinedPeriod(int year, int month, int day) {
		super();
		this.type = PredefinedPeriodType.DAY;
		month = month - 1; // January is equals to 0
		calendarFrom = new GregorianCalendar(year, month, day);
		calendarTo = null;
	}

	/**
	 * Constructs an open predefined period in hours from the given parameters.
	 * Final time will be null.
	 * 
	 * @param year Initial year.
	 * @param month Initial month.
	 * @param day Initial day.
	 * @param hour Initial hour.
	 */
	public PredefinedPeriod(int year, int month, int day, int hour) {
		super();
		this.type = PredefinedPeriodType.HOUR;
		month = month - 1; // January is equals to 0
		int minute = 1;
		calendarFrom = new GregorianCalendar(year, month, day, hour, minute);
		calendarTo = null;
	}
	
	/**
	 * Constructs a predefined period between two dates.
	 * Commonly used for less than an hour period.
	 * The type will be LESS_THAN_HOUR.
	 * @see PredefinedPeriodType
	 * @param from Initial date.
	 * @param to Final date.
	 */
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
	
	/**
	 * Returns an open PredefinedPeriod instance from a given key.
	 * @param periodKey String with period information separated by "-".
	 * @return An open PredefinedPeriod from the given key. 
	 */
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
	
	/**
	 * Return type of predefined period.
	 * @see PredefinedPeriodType
	 * @return Type of predefinedPeriod.
	 */
	public PredefinedPeriodType getType() {
		return type;
	}
    
	/**
	 * Returns the initial time of the predefined period.
	 * @return Initial time of the predefined period.
	 */
	public Calendar getCalendarFrom() {
		return this.calendarFrom;
	}
	
	/**
	 * Returns the final time of the predefined period.
	 * @return Final time of the predefined period.
	 */
	public Calendar getCalendarTo() {
		return this.calendarTo;
	}
	
	/**
	 * Returns a string that represents the initial time of this period.
	 * @return A string with the key of the initial time of this period.
	 */
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
				logger.warn("Predefined type is not well defined.");
				break;
		}
		return ret; 		
	}
	
	/**
	 * Returns a LocalDateTime object with the initial time of this period.
	 * @return A LocalDateTime object with the initial time of this period.
	 */
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
	
	/**
	 * Compares two predefinedPeriod objects. The comparison is done by the 
	 * lexicographical comparison of period keys.
	 * @param another PredefinedPeriod to compare with.
	 * @return <code>TRUE</code> if the predefined periods are equals, 
	 * <code>FALSE</code> otherwise. 
	 */
	public boolean equals(Object o){
		if (o instanceof PredefinedPeriod) {
			PredefinedPeriod another = (PredefinedPeriod) o; 
			return(getKey().equalsIgnoreCase(another.getKey()));
		} else {
			return false;
		}
	}
	
}
