package com.advicetec.utils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PeriodUtils 
{
	static int HOURSPERDAY = 24;
	static int MAX_MINUTES = 59;
	static int MAX_SECONDS = 59;
	static int HOURSPERMONTH = HOURSPERDAY * 30;
	static int HOURSPERYEAR = HOURSPERMONTH * 12;
	static Logger logger = LogManager.getLogger(PeriodUtils.class.getName()); 
	
	static public LocalDateTime getStartNextHour(LocalDateTime date) {
		LocalDateTime startNextHour = LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(), MAX_MINUTES, MAX_SECONDS );
		return startNextHour.plusSeconds(1);
	}
	
	static public LocalDateTime getPreviousFinalHour(LocalDateTime date) {
		LocalDateTime temp = LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(), MAX_MINUTES, MAX_SECONDS, 0 );
		return temp.minusHours(1);
	}
	
	static public LocalDateTime getPreviousFinalDay(LocalDateTime date){
		LocalDateTime temp  = LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 1, 1, 1, 0 );
		return temp = temp.minusSeconds(1);		
	}
	
	static public LocalDateTime getStartNextDay(LocalDateTime date) {
		LocalDateTime startNextDay = date.with(LocalTime.MAX);
		return startNextDay.plusNanos(1);
	}
	
	static public LocalDateTime getStartOfDay(LocalDateTime date) {
		LocalDateTime startNextDay = date.with(LocalTime.MIN);
		return startNextDay;		
	}
	
	static public LocalDateTime getStartNextMonth(LocalDateTime date) {
		LocalDateTime temp = LocalDateTime.of(date.getYear(), date.getMonthValue(), 1, 0, 0, 0 );
		return temp.plusMonths(1);
	}
	
	static public LocalDateTime getPreviousFinalMonth(LocalDateTime date) {
		LocalDateTime temp = LocalDateTime.of(date.getYear(), date.getMonthValue(), 1, 0, 0, 0 );
		return temp.minusSeconds(1);
	}
	
	static public LocalDateTime getStartNextYear(LocalDateTime date) {
		LocalDateTime temp = LocalDateTime.of(date.getYear(), 1, 1, 0, 0, 0 );
		return temp.plusYears(1);
	}
	
	static public LocalDateTime getPreviousFinalYear(LocalDateTime date) {
		LocalDateTime temp = LocalDateTime.of(date.getYear(), 1, 1, 0, 0, 0 );
		return temp.minusSeconds(1);
	}
	
	static public List<PredefinedPeriod> getPredefinedPeriodHours( LocalDateTime from, LocalDateTime to )
	{
		
		logger.info("from:" + from + " to:" + to);
		
		List<PredefinedPeriod> ret = new ArrayList<PredefinedPeriod>();

		LocalDateTime startNext = getStartNextHour(from);
		LocalDateTime previousEnd = getPreviousFinalHour(to);		
		previousEnd = previousEnd.plusSeconds(1);
		
		long hours = ChronoUnit.HOURS.between(startNext, previousEnd);
		
		logger.info("startNext:" + startNext + " previousEnd:" + previousEnd);
		logger.info("in getPredefinedPeriodHours  NumHours:" + Long.toString(hours));
		
		if (hours <= 0) {
			// Inserts the initial hour
			PredefinedPeriod predefinedPeriod = new PredefinedPeriod(from, to );
			ret.add(predefinedPeriod);
		
		} else {
		
			PredefinedPeriod predefinedPeriod = new PredefinedPeriod(from, startNext );
			ret.add(predefinedPeriod);
			
			for (int i = 0; i < hours; i++) { 
				predefinedPeriod = new PredefinedPeriod((int)startNext.getYear(), (int) startNext.getMonth().getValue(), 
																		(int)startNext.getDayOfMonth(), startNext.getHour() + i);
				ret.add(predefinedPeriod);
			}
			
			if (previousEnd.isBefore(to)){
				predefinedPeriod = new PredefinedPeriod(previousEnd, to );
				ret.add(predefinedPeriod);
			}
		}
		return ret;
		
	}
	
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


	static List<PredefinedPeriod> getPredefinedPeriods(LocalDateTime from, LocalDateTime to )
	{
		
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

}
