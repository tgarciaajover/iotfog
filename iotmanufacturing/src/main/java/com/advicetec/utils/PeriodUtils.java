package com.advicetec.utils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class PeriodUtils 
{
	static int HOURSPERDAY = 24;
	static int HOURSPERMONTH = HOURSPERDAY * 30;
	static int HOURSPERYEAR = HOURSPERMONTH * 12;
	
	static private LocalDateTime getStartNextDay(LocalDateTime date) {
		LocalDateTime startNextDay = date.with(LocalTime.MAX);
		startNextDay.plusSeconds(1);
		return startNextDay;
	}
	
	static private LocalDateTime getStartOfDay(LocalDateTime date) {
		LocalDateTime startNextDay = date.with(LocalTime.MIN);
		return startNextDay;		
	}
	
	static private LocalDateTime getStartNextMonth(LocalDateTime date) {
		LocalDateTime temp = LocalDateTime.of(date.getYear(), date.getMonthValue(), 0, 0, 0, 0 );
		temp.plusMonths(1);
		return temp;
	}
	
	static private LocalDateTime getPreviousFinalMonth(LocalDateTime date) {
		LocalDateTime temp = LocalDateTime.of(date.getYear(), date.getMonthValue(), 0, 0, 0, 0 );
		temp.minusSeconds(1);
		return temp;
	}
	
	static private LocalDateTime getStartNextYear(LocalDateTime date) {
		LocalDateTime temp = LocalDateTime.of(date.getYear(), 0, 0, 0, 0, 0 );
		temp.plusYears(1);
		return temp;
	}
	
	static private LocalDateTime getPreviousFinalYear(LocalDateTime date) {
		LocalDateTime temp = LocalDateTime.of(date.getYear(), 0, 0, 0, 0, 0 );
		temp.minusYears(1);
		return temp;
	}
	
	static private List<PredefinedPeriod> getPredefinedPeriodHours( LocalDateTime from, LocalDateTime to, int hours )
	{
		List<PredefinedPeriod> ret = new ArrayList<PredefinedPeriod>();

		for (int i = 1; i <= hours; i++) { 
			LocalDateTime temp = LocalDateTime.of(from.getYear(),from.getMonth(),from.getDayOfMonth(), from.getHour() + i, 0);
			PredefinedPeriod predefinedPeriod = new PredefinedPeriod((int)temp.getYear(), (int) temp.getMonth().getValue(), 
																	(int)temp.getDayOfMonth(), temp.getHour() );
			ret.add(predefinedPeriod);
		}
		
		return ret;
		
	}
	
	static private List<PredefinedPeriod> getPredefinedPeriodDays(LocalDateTime from, LocalDateTime to)
	{
		List<PredefinedPeriod> ret = new ArrayList<PredefinedPeriod>();

		while ( from.isBefore(to)) { 
			PredefinedPeriod predefinedPeriod = new PredefinedPeriod(from.getYear(), from.getMonth().getValue(), 
																	from.getDayOfMonth());
			ret.add(predefinedPeriod);
			from.plusDays(1);
		}
		
		return ret;		
	}
	
	static private List<PredefinedPeriod> getPredefinedPeriodMonths(LocalDateTime from, LocalDateTime to)
	{
		List<PredefinedPeriod> ret = new ArrayList<PredefinedPeriod>();

		while ( from.isBefore(to)) { 
			PredefinedPeriod predefinedPeriod = new PredefinedPeriod(from.getYear(), from.getMonth().getValue());
			ret.add(predefinedPeriod);
			from.plusMonths(1);
		}
		
		return ret;
		
	}

	static private List<PredefinedPeriod> getPredefinedPeriodYears(LocalDateTime from, LocalDateTime to)
	{
		List<PredefinedPeriod> ret = new ArrayList<PredefinedPeriod>();

		while ( from.isBefore(to)) { 
			PredefinedPeriod predefinedPeriod = new PredefinedPeriod(from.getYear());
			ret.add(predefinedPeriod);
			from.plusYears(1);
		}
		
		return ret;		
	}


	static List<PredefinedPeriod> getPredefinedPeriods(LocalDateTime from, LocalDateTime to )
	{
		
		List<PredefinedPeriod> ret = new ArrayList<PredefinedPeriod>();
		
		long hours = ChronoUnit.HOURS.between(from, to);
		
		if ( hours < HOURSPERDAY) {
			 
			ret.addAll(getPredefinedPeriodHours( from, to, (int) hours ));
			
		} else if ((hours >= HOURSPERDAY) && (hours <= HOURSPERMONTH )) {
			
			LocalDateTime startDay = getStartNextDay(from);
			LocalDateTime endDay = getStartOfDay(to);
			hours = ChronoUnit.HOURS.between(from, startDay);
			ret.addAll(getPredefinedPeriodHours( from , startDay, (int) hours ));

			// Extracts whole days
			ret.addAll(getPredefinedPeriodDays( startDay, endDay));
			
			// Extracts the final hours until the last day.
			hours = ChronoUnit.HOURS.between(endDay, to);
			ret.addAll(getPredefinedPeriodHours( from , startDay, (int) hours ));
			
		} else if ((hours > HOURSPERMONTH) && (hours <= HOURSPERMONTH )) {
			LocalDateTime startDay = getStartNextDay(from);
			LocalDateTime startNextMonth = getStartNextMonth(from);
			LocalDateTime endPreviousFinalMonth = getPreviousFinalMonth(to);
			LocalDateTime endDay = getStartOfDay(to);
			
			hours = ChronoUnit.HOURS.between(from, startDay);
			ret.addAll(getPredefinedPeriodHours( from , startDay, (int) hours ));

			// Extracts days until the next month
			ret.addAll(getPredefinedPeriodDays( startDay, startNextMonth));
			
			// Extracts whole months.
			ret.addAll(getPredefinedPeriodMonths(startNextMonth, endPreviousFinalMonth));
			
			// Extract days until the last day
			ret.addAll(getPredefinedPeriodDays( endPreviousFinalMonth, endDay));
			
			// Extract hours until the end datetime.
			hours = ChronoUnit.HOURS.between(endDay, to);
			ret.addAll(getPredefinedPeriodHours( endDay , to, (int) hours ));
			
		} else {
			LocalDateTime startDay = getStartNextDay(from);
			LocalDateTime startNextMonth = getStartNextMonth(from);
			LocalDateTime startNextYear = getStartNextYear(from);
			
			LocalDateTime endPreviousYear = getPreviousFinalYear(to);
			LocalDateTime endPreviousFinalMonth = getPreviousFinalMonth(to);
			LocalDateTime endDay = getStartOfDay(to);
			
			hours = ChronoUnit.HOURS.between(from, startDay);
			ret.addAll(getPredefinedPeriodHours( from , startDay, (int) hours ));

			// Extracts days until the next month
			ret.addAll(getPredefinedPeriodDays( startDay, startNextMonth));
			
			// Extract months until the end of the first year
			ret.addAll(getPredefinedPeriodMonths(startNextMonth, startNextYear));
			
			ret.addAll(getPredefinedPeriodYears(startNextYear, endPreviousYear));
			
			// Extract months from the end year until the end month
			ret.addAll(getPredefinedPeriodMonths(endPreviousYear, endPreviousFinalMonth));
			
			// Extract days until the last day
			ret.addAll(getPredefinedPeriodDays( endPreviousFinalMonth, endDay));
			
			// Extract hours until the end datetime.
			hours = ChronoUnit.HOURS.between(endDay, to);
			ret.addAll(getPredefinedPeriodHours( endDay , to, (int) hours ));
			
		}
		
		return ret; 
		
	}
}
