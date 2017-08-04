package com.advicetec.eventprocessor;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.measuredentitity.MeasuredEntityType;

public class OEEAggregationEventTest {

	@Test
	public void OEEGeneralAggregationTest(){
		
		String recurrence = "RRULE:FREQ=DAILY";
		
		OEEAggregationEventProcessor oeeEventProcessor = 
				new OEEAggregationEventProcessor(new AggregationEvent(111,
						MeasuredEntityType.MACHINE, AggregationEventType.OEE, recurrence));
		try {
			
			List<DelayEvent> list = oeeEventProcessor.process();
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
		
}
