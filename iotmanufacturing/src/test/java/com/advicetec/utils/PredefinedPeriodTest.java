package com.advicetec.utils;

import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.advicetec.displayadapter.TextFormat;

public class PredefinedPeriodTest 
{

	private static final String DATE_PATTERN = "MM/yyyy";

	public int getLastDayOfMonth(String dateString) {
	    DateTimeFormatter pattern = DateTimeFormatter.ofPattern(DATE_PATTERN);
	    YearMonth yearMonth = YearMonth.parse(dateString, pattern);
	    LocalDate date = yearMonth.atEndOfMonth();
	    return date.lengthOfMonth();
	}
	
	
	public void verifyHour(int year, int month, int day, Map<String, String> keys)
	{
		int hourFrom = 0;
		int hourTo = 23;

		for ( int i = hourFrom; i <= hourTo; i++) {  
			PredefinedPeriod predefinedPeriod = new PredefinedPeriod(year,month,day, i);
			String key = new String(predefinedPeriod.getKey());
			System.out.println("Hour key:" + key);
			assertTrue(keys.get(key)==null);
			keys.put(key, key);
		}
		
	}
	
	public void verifyDay(int year, int month, Map<String, String> keys)
	{
		String dateString = String.format("%02d", month) + "/" + Integer.toString(year); 
		
		int dayFrom = 1;
		int dayTo = getLastDayOfMonth(dateString);

		for ( int i = dayFrom; i <= dayTo; i++) {  
			PredefinedPeriod predefinedPeriod = new PredefinedPeriod(year,month,i);
			String key = new String(predefinedPeriod.getKey());
			System.out.println("Year:" + year + " month:" + month + " Day:" + i +" key:" + key);
			assertTrue(keys.get(key)==null);
			keys.put(key, key);
			verifyHour(year, month, i, keys);
		}
		
	}
	
	public void verifyMonth(int year, Map<String, String> keys)
	{

		int monthFrom = 1;
		int monthTo = 12;
		
		for ( int i = monthFrom; i <= monthTo; i++) {  
			PredefinedPeriod predefinedPeriod = new PredefinedPeriod(year,i);
			String key = new String(predefinedPeriod.getKey());
			System.out.println("Year:" + year + " month:" + i + " key:" + key);
			assertTrue(keys.get(key)==null);
			keys.put(key, key);
			verifyDay(year, i, keys);
		}

	}
	
	
	@Test
	public void PredefinedPeriodTest()
	{
		
		int yearFrom = 2010;
		int yearTo = 2016;
		Map<String, String> keys = new HashMap<String,String>();
		
		for ( int i = yearFrom; i <= yearTo; i++) {  
			PredefinedPeriod predefinedPeriod = new PredefinedPeriod(i);
			String key = predefinedPeriod.getKey();
			System.out.println("year key:" + key);
			// assertTrue(keys.get(key)==null);
			keys.put(key, key);
			verifyMonth(i, keys);
		}
		
	}
}
