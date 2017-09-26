package com.advicetec.utils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utilities class for periods of time.
 * 
 * @author maldofer
 *
 */
public class PeriodUtils 
{
	static int MAX_MINUTES = 59;
	static int MAX_SECONDS = 59;
	static Logger logger = LogManager.getLogger(PeriodUtils.class.getName());
	
	public static int SECONDSPERHOUR = 3600;
	public static int HOURSPERDAY = 24;
	public static int HOURSPERMONTH = HOURSPERDAY * 30;
	public static int HOURSPERYEAR = HOURSPERMONTH * 12;
	 
	/**
	 * Returns start of the next hour from the given date-time.
	 * @param date Date-Time
	 * @return A Date-Time with the next hour.
	 */
	static public LocalDateTime getStartNextHour(LocalDateTime date) {
		LocalDateTime startNextHour = LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(), MAX_MINUTES, MAX_SECONDS );
		return startNextHour.plusSeconds(1);
	}
	
	/**
	 * Returns the final of the previous hour from the given date-time.
	 * @param date Date-Time
	 * @return A Date-Time with the previour hour.
	 */
	static public LocalDateTime getPreviousFinalHour(LocalDateTime date) {
		LocalDateTime temp = LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(), MAX_MINUTES, MAX_SECONDS, 0 );
		return temp.minusHours(1);
	}
	
	/**
	 * Returns the final time of the previous day from the given date-time.
	 * @param date Date-Time
	 * @return The final time of the previous day.
	 */
	static public LocalDateTime getPreviousFinalDay(LocalDateTime date){
		LocalDateTime temp  = LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 1, 1, 1, 0 );
		return temp = temp.minusSeconds(1);		
	}
	
	/**
	 * Returns the start time of the next day from the given date-time.
	 * @param date Date-Time
	 * @return The start time for the next day.
	 */
	static public LocalDateTime getStartNextDay(LocalDateTime date) {
		LocalDateTime startNextDay = date.with(LocalTime.MAX);
		return startNextDay.plusNanos(1);
	}
	
	/**
	 * Returns the start time of the given date-time.
	 * @param date Date-Time
	 * @return The start time of the given date.
	 */
	static public LocalDateTime getStartOfDay(LocalDateTime date) {
		LocalDateTime startNextDay = date.with(LocalTime.MIN);
		return startNextDay;		
	}
	
	/**
	 * Returns the time of start the next month from the given date-time.
	 * @param date Date-Time
	 * @return The start time for the next month.
	 */
	static public LocalDateTime getStartNextMonth(LocalDateTime date) {
		LocalDateTime temp = LocalDateTime.of(date.getYear(), date.getMonthValue(), 1, 0, 0, 0 );
		return temp.plusMonths(1);
	}
	
	/**
	 * Returns the final time from of the previous month for the given date-time.
	 * @param date Date-Time
	 * @return The final time form the previous month.
	 */
	static public LocalDateTime getPreviousFinalMonth(LocalDateTime date) {
		LocalDateTime temp = LocalDateTime.of(date.getYear(), date.getMonthValue(), 1, 0, 0, 0 );
		return temp.minusSeconds(1);
	}
	
	/**
	 * Returns the start time from of the next year for the given date-time.
	 * @param date Date-Time
	 * @return The start time form the next year.
	 */
	static public LocalDateTime getStartNextYear(LocalDateTime date) {
		LocalDateTime temp = LocalDateTime.of(date.getYear(), 1, 1, 0, 0, 0 );
		return temp.plusYears(1);
	}
	
	/**
	 * Returns the final time from of the previous year for the given date-time.
	 * @param date Date-Time
	 * @return The final time form the previous year.
	 */
	static public LocalDateTime getPreviousFinalYear(LocalDateTime date) {
		LocalDateTime temp = LocalDateTime.of(date.getYear(), 1, 1, 0, 0, 0 );
		return temp.minusSeconds(1);
	}
	
	/**
	 * Returns a list of predefined periods in hours between the initial 
	 * time and end time.
	 * 
	 * @param from Initial time.
	 * @param to End time.
	 * @return List of predefined periods in hours after initial time and 
	 * before end time.
	 * 
	 * @see PredefinedPeriod
	 */
	static public List<PredefinedPeriod> getPredefinedPeriodHours( LocalDateTime from, LocalDateTime to )
	{
		
		logger.info("getPredefinedPeriodHours from:" + from + " to:" + to);
		
		List<PredefinedPeriod> ret = new ArrayList<PredefinedPeriod>();

		LocalDateTime startNext = getStartNextHour(from);
		LocalDateTime previousEnd = getPreviousFinalHour(to);		
		previousEnd = previousEnd.plusSeconds(1);
		// calculates the number of hours between initial and end dates.
		long hours = ChronoUnit.HOURS.between(startNext, previousEnd);
		
		logger.info("startNext:" + startNext + " previousEnd:" + previousEnd);
		logger.info("in getPredefinedPeriodHours  NumHours:" + Long.toString(hours));
		// if the difference is less than an hour
		if (hours <= 0) {
			// Inserts the initial hour
			PredefinedPeriod predefinedPeriod = new PredefinedPeriod(from, to );
			ret.add(predefinedPeriod);
		
		} else {
			// inserts the less than an hour period.
			PredefinedPeriod predefinedPeriod = new PredefinedPeriod(from, startNext );
			ret.add(predefinedPeriod);
			// inserts the periods of hours
			for (int i = 0; i < hours; i++) { 
				predefinedPeriod = new PredefinedPeriod((int)startNext.getYear(), (int) startNext.getMonthValue(), 
																		(int)startNext.getDayOfMonth(), startNext.getHour() + i);
				ret.add(predefinedPeriod);
			}
			// inserts the last less than an hour period.
			if (previousEnd.isBefore(to)){
				predefinedPeriod = new PredefinedPeriod(previousEnd, to );
				ret.add(predefinedPeriod);
			}
		}
		return ret;
	}
	
	/**
	 * Returns a list of predefined periods in days between initial 
	 * time and end time.
	 * 
	 * @param from Initial time.
	 * @param to End time.
	 * @return List of predefined periods in days between initial time and end time.
	 * 
	 * @see PredefinedPeriod
	 */
	static private List<PredefinedPeriod> getPredefinedPeriodDays(LocalDateTime from, LocalDateTime to)
	{
		List<PredefinedPeriod> ret = new ArrayList<PredefinedPeriod>();

		logger.info("from date:" + from + " to date:" + to);
		
		LocalDateTime temp = from; 
		while ( temp.isBefore(to)) { 
			PredefinedPeriod predefinedPeriod = new PredefinedPeriod(temp.getYear(), temp.getMonth().getValue(), 
																	temp.getDayOfMonth());
			ret.add(predefinedPeriod);
			temp = temp.plusDays(1);
			logger.debug("temp:" + temp);
		}
		
		logger.info("number of registers:" + ret.size());
		return ret;		
	}
	
	/**
	 * Returns a list of predefined periods in months between initial 
	 * time and end time.
	 * @param from Initial time.
	 * @param to End time.
	 * @return List of predefined periods in months between initial time and end time.
	 * 
	 * @see PredefinedPeriod
	 */
	static private List<PredefinedPeriod> getPredefinedPeriodMonths(LocalDateTime from, LocalDateTime to)
	{
		List<PredefinedPeriod> ret = new ArrayList<PredefinedPeriod>();

		logger.info("getPredefinedPeriodMonths from:" + from + " to:" + to);
		
		LocalDateTime temp = from;
		while ( temp.isBefore(to)) { 
			PredefinedPeriod predefinedPeriod = new PredefinedPeriod(temp.getYear(), temp.getMonth().getValue() );
			ret.add(predefinedPeriod);
			temp = temp.plusMonths(1);
		}
		
		logger.debug("getPredefinedPeriodMonths end result");
		for (int i = 0; i < ret.size(); i++) {
			logger.debug(ret.get(i).getKey());
		}
		
		return ret;
		
	}

	/**
	 * Returns a list of predefined periods in years between initial 
	 * time and end time.
	 * 
	 * @param from Initial time.
	 * @param to End time.
	 * @return List of predefined periods in years between initial time and end time.
	 * 
	 * @see PredefinedPeriod
	 */
	static private List<PredefinedPeriod> getPredefinedPeriodYears(LocalDateTime from, LocalDateTime to)
	{
		List<PredefinedPeriod> ret = new ArrayList<PredefinedPeriod>();

		logger.info("getPredefinedPeriodYears from:" + from + " to" + to );
		
		LocalDateTime temp = from;
		while ( temp.isBefore(to)) { 
			PredefinedPeriod predefinedPeriod = new PredefinedPeriod(temp.getYear());
			ret.add(predefinedPeriod);
			temp = temp.plusYears(1);
		}
		
		logger.info("getPredefinedPeriodYears result count:" + ret.size() );
		
		return ret;		
	}

	/**
	 * Returns a list of predefined periods between initial time and end time.
	 * Default periods depend on the given parameters. 
	 * If the period denotes less than a day, the period is defined in hours.
	 * If the period denotes more than a day and less than a month, the period is defined in days.
	 * If the period denotes more than a month and less than a year, the period is defined in months.
	 * Otherwise, the period is defined in months.
	 * Incomplete periods, from the initial time to first complete period 
	 * and from the last complete period to the final time, are also 
	 * interpreted as default periods.
	 * 
	 * @param from Initial time.
	 * @param to End time.
	 * @return List of predefined periods by default between initial time and end time.
	 * 
	 * @see PredefinedPeriod
	 */
	public static List<PredefinedPeriod> getPredefinedPeriodsDefault(LocalDateTime from, LocalDateTime to){

		List<PredefinedPeriod> ret = new ArrayList<PredefinedPeriod>();
		
		long hours = ChronoUnit.HOURS.between(from, to);
		
		logger.info("Number of hours:" + Long.toString(hours));
		
		if ( hours < HOURSPERDAY) {
			 			
			ret = getPredefinedPeriodHours( from , to);
			
		} else if ((hours >= HOURSPERDAY) && (hours <= HOURSPERMONTH )) {
			
			logger.info("hours between days and months");
			
			LocalDateTime startDay = getStartNextDay(from);
			LocalDateTime endDay = getStartOfDay(to);
			ret.addAll(getPredefinedPeriodHours( from , startDay ));

			// Extracts whole days
			ret.addAll(getPredefinedPeriodDays( startDay, endDay));
			
			// Extracts the final hours until the last day.
			ret.addAll(getPredefinedPeriodHours( endDay , to));
			
		} else if ((hours > HOURSPERMONTH) && (hours <= HOURSPERYEAR )) {
			
			logger.info("hours between months and years");
			
			LocalDateTime startNextDay = getStartNextDay(from);
			LocalDateTime startNextMonth = getStartNextMonth(from);
			LocalDateTime endPreviousFinalMonth = getPreviousFinalMonth(to);
			LocalDateTime endDay = getStartOfDay(to);
			
			ret.addAll(getPredefinedPeriodHours( from , startNextDay));

			// Extracts days until the next month
			ret.addAll(getPredefinedPeriodDays( startNextDay, startNextMonth.minusSeconds(1)));
			
			// Extracts whole months.
			ret.addAll(getPredefinedPeriodMonths(startNextMonth, endPreviousFinalMonth));
			
			// Extract days until the last day
			ret.addAll(getPredefinedPeriodDays( endPreviousFinalMonth.plusSeconds(1), endDay));
			
			// Extract hours until the end datetime.
			ret.addAll(getPredefinedPeriodHours( endDay , to ));
			
		} else {

			logger.info("hours more than years");
			
			LocalDateTime startDay = getStartNextDay(from);
			LocalDateTime startNextMonth = getStartNextMonth(from);
			LocalDateTime startNextYear = getStartNextYear(from);
			
			LocalDateTime endPreviousYear = getPreviousFinalYear(to);
			LocalDateTime endPreviousFinalMonth = getPreviousFinalMonth(to);
			LocalDateTime endDay = getStartOfDay(to);
			
			ret.addAll(getPredefinedPeriodHours( from , startDay ));

			// Extracts days until the next month
			ret.addAll(getPredefinedPeriodDays( startDay, startNextMonth.minusSeconds(1)));
			
			// Extract months until the end of the first year
			ret.addAll(getPredefinedPeriodMonths(startNextMonth, startNextYear));
			
			ret.addAll(getPredefinedPeriodYears(startNextYear, endPreviousYear));
			
			// Extract months from the end year until the end month
			ret.addAll(getPredefinedPeriodMonths(endPreviousYear.plusSeconds(1), endPreviousFinalMonth));
			
			// Extract days until the last day
			ret.addAll(getPredefinedPeriodDays( endPreviousFinalMonth.plusSeconds(1), endDay));
			
			// Extract hours until the end datetime.
			ret.addAll(getPredefinedPeriodHours( endDay.plusSeconds(1) , to));
			
		}
		
		return ret; 
	}

	/**
	 * Returns a list of PredefinedPeriods between the initial and final times.
	 * The period type is given by the <code>reqInterval</code> parameter.
	 * 
	 * @param from Initial time.
	 * @param to Final time.
	 * @param reqInterval Required interval; "H" for hours, "D" for days, and 
	 * "M" for minutes. Otherwise <i>default</i> interval is used.
	 * @return A list of PredefinedPeriods between the initial and final dates.
	 */
	public static List<PredefinedPeriod> getPredefinedPeriods(LocalDateTime from, LocalDateTime to, String reqInterval )
	{
				
		if (reqInterval.compareTo("H") == 0){
			return getPredefinedPeriodHours( from , to );
		} else if(reqInterval.compareTo("D") == 0) { 
			return getPredefinedPeriodDays( from , to );
		} else if (reqInterval.compareTo("M") == 0) {
			return getPredefinedPeriodMonths( from , to );
		} else {
			return getPredefinedPeriodYears( from , to );
		}
	}

}
