package com.advicetec.configuration.test;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.IOSignalDeviceType;
import com.advicetec.configuration.Signal;
import com.advicetec.configuration.SignalContainer;
import com.advicetec.configuration.SignalType;
import com.advicetec.configuration.SignalUnit;
import com.advicetec.configuration.SignalUnitContainer;

public class IOSignalDeviceTypeTest 
{
	@Test
	public void Test_IO_Signal_Device_Type() 
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
		
		IOSignalDeviceType ioSignal = new IOSignalDeviceType(3);
		ioSignal.setI_O("asdads");
		ioSignal.setSignal(signal);
		
		String jsonText = ioSignal.toJson();
		System.out.println("IOSignal" + jsonText);
		
	}
}
