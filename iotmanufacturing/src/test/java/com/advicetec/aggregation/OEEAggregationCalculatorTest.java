package com.advicetec.aggregation;

import java.sql.SQLException;
import java.time.LocalDateTime;

import org.junit.Test;

import com.advicetec.aggregation.oee.OEEAggregationCalculator;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.eventprocessor.AggregationEvent;
import com.advicetec.eventprocessor.AggregationEventType;
import com.advicetec.eventprocessor.OEEAggregationEventProcessor;
import com.advicetec.measuredentitity.MeasuredEntityType;

public class OEEAggregationCalculatorTest 
{

	@Test
	public void OEEhourAggregationTest() {

		ConfigurationManager manager = ConfigurationManager.getInstance();
		OEEAggregationCalculator oeeAggregationCalculator = new OEEAggregationCalculator(); 
		try {
			
			manager.loadConfiguration();
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		int owner = 2;
		LocalDateTime formerHour = LocalDateTime.of(2017, 07, 10, 5, 0);
		oeeAggregationCalculator.calculateHour(owner, MeasuredEntityType.MACHINE, formerHour, true, false);

	}
	
	@Test
	public void OEEDayAggregationTest() {
		
		OEEAggregationCalculator oeeAggregationCalculator = new OEEAggregationCalculator(); 
		ConfigurationManager manager = ConfigurationManager.getInstance();
		try {
			
			manager.loadConfiguration();
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		int owner = 2;
		LocalDateTime day = LocalDateTime.of(2017, 07, 10, 0, 0);
		oeeAggregationCalculator.calculateDay(owner, MeasuredEntityType.MACHINE, day, true, false);
	}	

	@Test
	public void OEEMonthAggregationTest() {
		
		OEEAggregationCalculator oeeAggregationCalculator = new OEEAggregationCalculator(); 
		ConfigurationManager manager = ConfigurationManager.getInstance();
		try {
			
			manager.loadConfiguration();
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		int owner = 2;
		LocalDateTime month = LocalDateTime.of(2017, 07, 1, 0, 0);
		oeeAggregationCalculator.calculateMonth(owner, MeasuredEntityType.MACHINE, month,true, false);

	}	

	@Test
	public void OEEYearAggregationTest() {
		
		OEEAggregationCalculator oeeAggregationCalculator = new OEEAggregationCalculator(); 
		ConfigurationManager manager = ConfigurationManager.getInstance();
		try {
			
			manager.loadConfiguration();
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		int owner = 2;
		LocalDateTime year = LocalDateTime.of(2017, 1, 1, 0, 0);
		oeeAggregationCalculator.calculateYear(owner, MeasuredEntityType.MACHINE, year, true, false);

	}	

	
}
