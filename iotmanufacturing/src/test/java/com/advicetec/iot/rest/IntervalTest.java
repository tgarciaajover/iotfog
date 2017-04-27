package com.advicetec.iot.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.time.LocalDateTime;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import com.advicetec.core.TimeInterval;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.MeasuringState;
import com.advicetec.measuredentitity.ReasonCode;
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
		ReasonCode reason = new ReasonCode("001", "Reason 001");
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
		ReasonCode reason = new ReasonCode("001", "Reason 001");
		
		StateInterval interval = new StateInterval(state, reason, int1, "P00",MeasuredEntityType.MACHINE);
		
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
}
