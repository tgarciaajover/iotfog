package com.advicetec.core.test;

import java.time.LocalDateTime;

import org.junit.Test;

import com.advicetec.configuration.SignalUnit;

public class SignalUnitTest 
{
	@Test
	public void Test_Signal_Unit() 
	{
		SignalUnit signal = new SignalUnit(new Integer(1));
		signal.setDescr("CYC");
		signal.setCreate_date(LocalDateTime.now());
		
		String jsonString = signal.toJson();
		System.out.println("signal unit" + jsonString);
		
		SignalUnit signal2 = new SignalUnit(new Integer(0));
		signal2.fromJSON(jsonString);
		
		System.out.println("json:" + signal2);
	}
}
