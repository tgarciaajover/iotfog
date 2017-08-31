package com.advicetec.eventprocessor;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.core.AttributeValue;
import com.advicetec.measuredentitity.MeasuredAttributeValue;
import com.advicetec.measuredentitity.MeasuredEntityManager;
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
	
	@Test
	public void AggregationInstalationTest() throws SQLException{
		
		String sqlText = "SELECT count(*) FROM setup_measuredentityscheduledevent";
		
		MeasuredEntityManager manager = MeasuredEntityManager.getInstance();
		
		Connection conn  = null; 
		PreparedStatement pst = null;

		String driver = manager.getProperty("driver");
		String server = manager.getProperty("server");
		String user = manager.getProperty("user");
		String password = manager.getProperty("password");
		ResultSet rs = null;
		
		Integer numEvents = new Integer(0);
		
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(server, user, password);
			conn.setAutoCommit(false);

			pst = conn.prepareStatement(sqlText);			
			rs =  pst.executeQuery();

			// Bring the attribute 
			while (rs.next())
			{
				numEvents = rs.getInt(1);
			}
			
			EventManager eventManager = EventManager.getInstance();
						
			Iterator<DelayEvent> iterator = eventManager.getDelayedQueue().iterator();
			while (iterator.hasNext()){
				DelayEvent evnt = iterator.next();
				
				if (evnt.getEvent().getEvntType() == EventType.AGGREGATION_EVENT){
					numEvents--;
				}
			}
			
			assertEquals("The number of events is not those configured - diference:", new Integer(0), numEvents );

			
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		finally{
			if(pst!=null)
			{
				try
				{
					pst.close();
				} catch (SQLException e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			}

			if(conn!=null) 
			{
				try
				{
					conn.close();
				} catch (SQLException e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			}
		}

		
	}
}
