package com.advicetec.iot.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.time.LocalDateTime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import com.advicetec.configuration.ReasonCode;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeType;
import com.advicetec.core.MeasuringUnit;
import com.advicetec.core.TimeInterval;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.MeasuringState;
import com.advicetec.measuredentitity.StateInterval;

public class IntervalTest {

	@Test
	public void jsonTimeInterval(){
		LocalDateTime s = LocalDateTime.of(2016, 12, 24, 23, 59);
		LocalDateTime e = LocalDateTime.now();
		TimeInterval int1 = new TimeInterval(s, e);
		String json = int1.toJson();
		System.out.println(json);
		System.out.println(int1);
		TimeInterval int2 = null;

		try {
			int2 = new ObjectMapper().readValue(json, TimeInterval.class);
			System.out.println(int2);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		assertEquals("They don't match.",int1.toString(),int2.toString());
	}

	@Test
	public void jsonReasonCode(){
		ReasonCode reason = new ReasonCode(1, "Reason 001");
		reason.setGroup("Operating");
		reason.setClassification("None");
		reason.setIdleDown(false);
		
		String json = reason.toJson();
		System.out.println(json);
		System.out.println(reason.toString());
		ReasonCode reason2 = null;

		try {
			reason2 = new ObjectMapper().readValue(json,ReasonCode.class);
			System.out.println(reason2);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		assertEquals("They don't match.",reason.toString(),reason2.toString());
	}
	
	
	@Test
	public void jsonMeasuringState(){
		MeasuringState state = MeasuringState.OPERATING;
		String json = state.toJson();
		System.out.println(json);
		System.out.println(state.toString());
		MeasuringState state2 = null;

		try {
			state2 = new ObjectMapper().readValue(json,MeasuringState.class);
			System.out.println(state2);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		assertEquals("They don't match.",state.toString(),state2.toString());
	}
	
	@Test
	public void jsonStateInterval(){
		
		LocalDateTime s = LocalDateTime.of(2016, 12, 24, 23, 59);
		LocalDateTime e = LocalDateTime.now();
		TimeInterval int1 = new TimeInterval(s, e);
		
		MeasuringState state = MeasuringState.OPERATING;
		ReasonCode reason = new ReasonCode(1, "Reason 001");
		reason.setGroup("Operating");
		reason.setClassification("None");
		reason.setIdleDown(false);
		
		Double rate = new Double(0.0);
		Double conversion1 = new Double(0.0);
		Double conversion2 = new Double(0.0);
		Double actualRate = new Double(0.0);
		Double qtyDefective = new Double(0.0);
		
		
		
		StateInterval interval = new StateInterval(state, reason, int1, 2,MeasuredEntityType.MACHINE, 1, MeasuredEntityType.JOB.getValue(), "key1", rate, conversion1, conversion2, actualRate, qtyDefective);
		
		String json = interval.toJson();
		System.out.println(json);
		System.out.println(interval.toString());
		StateInterval state2 = null;

		try {
			state2 = new ObjectMapper().readValue(json,StateInterval.class);
			System.out.println(state2);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		assertEquals("They don't match.",interval.toString(),state2.toString());
	}
	
	private StateInterval createSchedShutdownStateInterval(TimeInterval int1)
	{
				
		MeasuringState state = MeasuringState.SCHEDULEDOWN;
		ReasonCode reason = new ReasonCode(2, "No sales orders");
		reason.setGroup("Shutdown");
		reason.setClassification("Sales");
		reason.setIdleDown(false);
		
		Double rate = new Double(0.0);
		Double conversion1 = new Double(0.0);
		Double conversion2 = new Double(0.0);
		Double actualRate = new Double(0.0);
		Double qtyDefective = new Double(0.0);
		
		StateInterval interval = new StateInterval(state, reason, int1, 2,MeasuredEntityType.MACHINE, 1, MeasuredEntityType.JOB.getValue(), "key1", rate, conversion1, conversion2, actualRate, qtyDefective);
		
		return interval;
		
	}

	private StateInterval createNonSchedShutdownStateInterval(TimeInterval int1)
	{
				
		MeasuringState state = MeasuringState.UNSCHEDULEDOWN;
		ReasonCode reason = new ReasonCode(3, "Electrical Failure");
		reason.setGroup("Shutdown");
		reason.setClassification("Electricity");
		reason.setIdleDown(true);
		
		Double rate = new Double(0.0);
		Double conversion1 = new Double(0.0);
		Double conversion2 = new Double(0.0);
		Double actualRate = new Double(0.0);
		Double qtyDefective = new Double(0.0);
		
		StateInterval interval = new StateInterval(state, reason, int1, 2,MeasuredEntityType.MACHINE, 1, MeasuredEntityType.JOB.getValue(), "key1", rate, conversion1, conversion2, actualRate, qtyDefective);
		
		return interval;
		
	}
	
	private StateInterval createOperationStateInterval(TimeInterval int1)
	{
				
		MeasuringState state = MeasuringState.OPERATING;
		ReasonCode reason = null;
		
		Double rate = new Double(0.0);
		Double conversion1 = new Double(0.0);
		Double conversion2 = new Double(0.0);
		Double actualRate = new Double(0.0);
		Double qtyDefective = new Double(0.0);
		
		StateInterval interval = new StateInterval(state, reason, int1, 2,MeasuredEntityType.MACHINE, 1, MeasuredEntityType.JOB.getValue(), "key1", rate, conversion1, conversion2, actualRate, qtyDefective);
		
		return interval;
		
	}
	
	
	@Test 
	public void saveDatabase() {
	
		LocalDateTime ldt1 = LocalDateTime.of(2017, 07, 10, 5, 5);
		LocalDateTime ldt2 = LocalDateTime.of(2017, 07, 10, 5, 6);
		LocalDateTime ldt3 = LocalDateTime.of(2017, 07, 10, 5, 7);
		LocalDateTime ldt4 = LocalDateTime.of(2017, 07, 10, 5, 8);
		TimeInterval int1 = new TimeInterval(ldt1, ldt2);
		TimeInterval int2 = new TimeInterval(ldt2, ldt3);
		TimeInterval int3 = new TimeInterval(ldt3, ldt4);

		StateInterval SInt1 = createSchedShutdownStateInterval(int1);
		StateInterval SInt2 = createNonSchedShutdownStateInterval(int2);
		StateInterval SInt3 = createOperationStateInterval(int3);
		
		Connection conn  = null; 
        PreparedStatement pst = null;
        PreparedStatement pst2 = null;
		
        try
        {
        	Class.forName("org.postgresql.Driver");
        	conn = DriverManager.getConnection("jdbc:postgresql://192.168.0.19:5432/iotajover", "iotajover", "iotajover");

        	conn.setAutoCommit(false);

        	String deleteSQL = StateInterval.SQL_Delete;

			pst = conn.prepareStatement(deleteSQL);
			
			SInt1.dbDelete(pst);
			SInt2.dbDelete(pst);
			SInt3.dbDelete(pst);
			
			pst.executeBatch();
        	
        	String insertSQL = StateInterval.SQL_Insert;
			
			pst2 = conn.prepareStatement(insertSQL);
			
			SInt1.dbInsert(pst2);
			SInt2.dbInsert(pst2);
			SInt3.dbInsert(pst2);
			
			pst2.executeBatch();
			
			conn.commit();
        	

        } catch(Exception e){
        	e.printStackTrace();
        } finally{
        	if(pst!=null)
        	{
        		try
        		{
        			pst.close();
        		} catch (SQLException e) {
        			e.printStackTrace();
        		}
        	}

        	if(conn!=null) 
        	{
        		try
        		{
        			conn.close();
        		} catch (SQLException e) {
        			e.printStackTrace();
        		}
        	}
        }

	}
}
