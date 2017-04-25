package com.advicetec.configuration.test;

import java.time.LocalDateTime;

import org.junit.Test;

import com.advicetec.configuration.Signal;
import com.advicetec.configuration.SignalType;
import com.advicetec.configuration.SignalUnit;

public class SignalTest 
{
	@Test
	public void Test_Signal_Unit() 
	{
		SignalUnit signalUnit = new SignalUnit(new Integer(1));
		signalUnit.setDescr("CYC");
		signalUnit.setCreate_date(LocalDateTime.now());
		
		SignalType type = new SignalType(1); 
		type.setName("Digital");
		type.setClassName("DigitalIO");
		
		Signal signal = new Signal(2);
		signal.setType(type);
		signal.setUnit(signalUnit);
		signal.setDescr("digital signal");
		signal.setCreate_date(LocalDateTime.now());
		
		
		String jsonString = signal.toJson();
		System.out.println("signal" + jsonString);
		
		
	}
}
