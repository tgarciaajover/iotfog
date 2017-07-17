package com.advicetec.utils;

import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import org.junit.Test;

public class PeriodUtilsTest 
{

	@Test
	public void PeriodUtilsGeneralFunctionsTest() {
		LocalDateTime date = LocalDateTime.of(2014, Month.JANUARY.getValue(), 10, 10, 1);
		
		LocalDateTime dTestRes = PeriodUtils.getStartNextHour(date);
		
		LocalDateTime dTest = LocalDateTime.of(2014, Month.JANUARY.getValue(), 10, 11, 0);
				
		assertTrue(dTest.equals(dTestRes) == true);
		
		dTest = LocalDateTime.of(2014, Month.JANUARY.getValue(), 10, 9, 59, 59);
		
		dTestRes = PeriodUtils.getPreviousFinalHour(date);
		
		assertTrue(dTest.equals(dTestRes) == true);
		
		dTest = LocalDateTime.of(2014, Month.JANUARY.getValue(), 11, 0, 0, 0);
		
		dTestRes = PeriodUtils.getStartNextDay(date);
				
		assertTrue(dTest.equals(dTestRes) == true);
		
		dTest = LocalDateTime.of(2014, Month.JANUARY.getValue(), 10, 0, 0, 0);
		
		dTestRes = PeriodUtils.getStartOfDay(date);
		
		assertTrue(dTest.equals(dTestRes) == true);
		
		dTest = LocalDateTime.of(2014, Month.FEBRUARY.getValue(), 1, 0, 0, 0);
		
		dTestRes = PeriodUtils.getStartNextMonth(date);

		assertTrue(dTest.equals(dTestRes) == true);
		
		dTest = LocalDateTime.of(2013, Month.DECEMBER.getValue(), 31, 23, 59, 59);
		
		dTestRes = PeriodUtils.getPreviousFinalMonth(date);
		
		assertTrue(dTest.equals(dTestRes) == true);
		
		dTest = LocalDateTime.of(2015, Month.JANUARY.getValue(), 1, 0, 0, 0);

		dTestRes = PeriodUtils.getStartNextYear(date);
		
		assertTrue(dTest.equals(dTestRes) == true);
		
		
	}
	
	
	@Test
	public void PeriodUtilsHoursTest()
	{
		LocalDateTime from = LocalDateTime.of(2014, Month.JANUARY.getValue(), 10, 10, 1);
		LocalDateTime to = LocalDateTime.of(2014, Month.JANUARY.getValue(), 10, 10, 35);
		
		List<PredefinedPeriod> periods = PeriodUtils.getPredefinedPeriods(from, to);
		assertTrue(periods.size()==1);
		
		for (int i = 0; i < periods.size(); i++) {
			System.out.println(periods.get(i).getKey());
		}
		
		LocalDateTime to2 = LocalDateTime.of(2014, Month.JANUARY.getValue(), 10, 12, 35);
		periods = PeriodUtils.getPredefinedPeriods(from, to2);
		assertTrue(periods.size()==3);

		// for (int i = 0; i < periods.size(); i++) {
		//	System.out.println(periods.get(i).getKey());
		// }

	}
	
	@Test
	public void PeriodUtilsDayTest()
	{
		LocalDateTime from = LocalDateTime.of(2014, Month.JANUARY.getValue(), 10, 10, 1);
		LocalDateTime to = LocalDateTime.of(2014, Month.JANUARY.getValue(), 11, 10, 35);
		
		List<PredefinedPeriod> periods = PeriodUtils.getPredefinedPeriods(from, to);
		System.out.println("Number of periods:" + periods.size());
		assertTrue(periods.size()==25);

		LocalDateTime to2 = LocalDateTime.of(2014, Month.JANUARY.getValue(), 12, 10, 35);
		
		periods = PeriodUtils.getPredefinedPeriods(from, to);
		System.out.println("Number of periods:" + periods.size());
		
		for (int i = 0; i < periods.size(); i++) {
			System.out.println(periods.get(i).getKey());
		}
		
		assertTrue(periods.size()==25);

	}
	
	
	@Test
	public void PeriodUtilsMonthTest()
	{
		LocalDateTime from = LocalDateTime.of(2014, Month.JANUARY.getValue(), 10, 10, 1);
		LocalDateTime to = LocalDateTime.of(2014, Month.FEBRUARY.getValue(), 11, 10, 35);
		
		List<PredefinedPeriod> periodsMonths = PeriodUtils.getPredefinedPeriods(from, to);
		
		System.out.println("Number of periods:" + periodsMonths.size());

		// for (int i = 0; i < periodsMonths.size(); i++) {
		//	System.out.println(periodsMonths.get(i).getKey());
		// }

		
		assertTrue(periodsMonths.size()==56);
		
	}

	@Test
	public void PeriodUtilsYearTest()
	{
		LocalDateTime from = LocalDateTime.of(2014, Month.JANUARY.getValue(), 10, 10, 1);
		LocalDateTime to = LocalDateTime.of(2015, Month.FEBRUARY.getValue(), 11, 10, 35);
		
		List<PredefinedPeriod> periods = PeriodUtils.getPredefinedPeriods(from, to);
		
		System.out.println("Number of periods:" + periods.size());

		//for (int i = 0; i < periods.size(); i++) {
		//	System.out.println(periods.get(i).getKey());
		// }
		
		assertTrue(periods.size()==68);
		
	}

	@Test
	public void PeriodUtilsYearsTest()
	{
		LocalDateTime from = LocalDateTime.of(2014, Month.JANUARY.getValue(), 10, 10, 1);
		LocalDateTime to = LocalDateTime.of(2016, Month.FEBRUARY.getValue(), 11, 10, 35);
		
		List<PredefinedPeriod> periods = PeriodUtils.getPredefinedPeriods(from, to);
		
		System.out.println("Number of periods:" + periods.size());

		//for (int i = 0; i < periods.size(); i++) {
		//	System.out.println(periods.get(i).getKey());
		//}
		
		assertTrue(periods.size()==69);
		
	}
	
}
