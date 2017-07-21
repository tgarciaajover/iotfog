package com.advicetec.eventprocessor;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.measuredentitity.MeasuredEntityType;

public class OEEAggregationEventTest {

	@Test
	public void OEEhourAggregationTest(){
		
		OEEAggregationEventProcessor oeeEventProcessor = 
				new OEEAggregationEventProcessor(new AggregationEvent(111,
						MeasuredEntityType.MACHINE, AggregationEventType.OEE));
		try {
			List<DelayEvent> deleayed = oeeEventProcessor.process();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
