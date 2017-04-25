package com.advicetec.configuration.test;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.SignalType;
import com.advicetec.configuration.SignalTypeContainer;

public class SignalTypeTest 
{
	@Test
	public void Test_Signal_Unit() 
	{
		SignalType signalType = new SignalType(new Integer(1));
		signalType.setName("Digital");
		signalType.setClassName("DigitalIO");
		
		String jsonString = signalType.toJson();
		
		ConfigurationManager instance = ConfigurationManager.getInstance();
		
		SignalTypeContainer container = new SignalTypeContainer(instance.getProperty("server"),
																instance.getProperty("user"),
																instance.getProperty("password")); 
		
		container.fromJSON(jsonString);
		SignalType signalType2 = (SignalType) container.getObject(1);
		
		assertEquals("Import from Json does not work,",signalType.toJson(), signalType2.toJson() );
		
	}
}
