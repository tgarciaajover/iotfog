package com.advicetec.configuration.test;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.SignalUnit;
import com.advicetec.configuration.SignalUnitContainer;

public class SignalUnitTest 
{
	@Test
	public void Test_Signal_Unit() 
	{
		SignalUnit signalUnit = new SignalUnit(new Integer(1));
		signalUnit.setDescr("CYC");
		signalUnit.setCreate_date(LocalDateTime.now());
		
		String jsonString = signalUnit.toJson();
		
		ConfigurationManager instance = ConfigurationManager.getInstance();
		
		SignalUnitContainer container = instance.getSignalUnitContainer(); 
		
		container.fromJSON(jsonString);
		SignalUnit signalUnit2 = (SignalUnit) container.getObject(1);
		
		assertEquals("Import from Json does not work,",signalUnit.toJson(), signalUnit2.toJson() );
		
		System.out.println(signalUnit2.toJson());
	}
}
