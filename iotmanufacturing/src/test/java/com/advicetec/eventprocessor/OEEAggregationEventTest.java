package com.advicetec.eventprocessor;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.ReasonCodeContainer;
import com.advicetec.measuredentitity.MeasuredEntityType;

public class OEEAggregationEventTest {

	@Test
	public void OEEGeneralAggregationTest(){
		
		OEEAggregationEventProcessor oeeEventProcessor = 
				new OEEAggregationEventProcessor(new AggregationEvent(111,
						MeasuredEntityType.MACHINE, AggregationEventType.OEE));
		try {
			
			List<DelayEvent> list = oeeEventProcessor.process();
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void OEEhourAggregationTest() {
		OEEAggregationEventProcessor oeeEventProcessor = 
				new OEEAggregationEventProcessor(new AggregationEvent(111,
						MeasuredEntityType.MACHINE, AggregationEventType.OEE));
		
		ConfigurationManager manager = ConfigurationManager.getInstance();
		try {
			
			manager.loadConfiguration();
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		int owner = 2;
		LocalDateTime formerHour = LocalDateTime.of(2017, 07, 10, 5, 0);
		oeeEventProcessor.calculateHour(owner, MeasuredEntityType.MACHINE, formerHour);

	}
	
	@Test
	public void OEEDayAggregationTest() {
		OEEAggregationEventProcessor oeeEventProcessor = 
				new OEEAggregationEventProcessor(new AggregationEvent(111,
						MeasuredEntityType.MACHINE, AggregationEventType.OEE));
		
		ConfigurationManager manager = ConfigurationManager.getInstance();
		try {
			
			manager.loadConfiguration();
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		int owner = 2;
		LocalDateTime day = LocalDateTime.of(2017, 07, 10, 0, 0);
		oeeEventProcessor.calculateDay(owner, MeasuredEntityType.MACHINE, day);

	}	

	@Test
	public void OEEMonthAggregationTest() {
		OEEAggregationEventProcessor oeeEventProcessor = 
				new OEEAggregationEventProcessor(new AggregationEvent(111,
						MeasuredEntityType.MACHINE, AggregationEventType.OEE));
		
		ConfigurationManager manager = ConfigurationManager.getInstance();
		try {
			
			manager.loadConfiguration();
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		int owner = 2;
		LocalDateTime month = LocalDateTime.of(2017, 07, 1, 0, 0);
		oeeEventProcessor.calculateMonth(owner, MeasuredEntityType.MACHINE, month);

	}	

	
	@Test
	public void OEEYearAggregationTest() {
		OEEAggregationEventProcessor oeeEventProcessor = 
				new OEEAggregationEventProcessor(new AggregationEvent(111,
						MeasuredEntityType.MACHINE, AggregationEventType.OEE));
		
		ConfigurationManager manager = ConfigurationManager.getInstance();
		try {
			
			manager.loadConfiguration();
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		int owner = 2;
		LocalDateTime year = LocalDateTime.of(2017, 1, 1, 0, 0);
		oeeEventProcessor.calculateYear(owner, MeasuredEntityType.MACHINE, year);

	}	
	
}
