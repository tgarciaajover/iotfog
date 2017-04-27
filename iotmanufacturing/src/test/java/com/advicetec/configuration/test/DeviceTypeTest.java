package com.advicetec.configuration.test;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.DeviceType;
import com.advicetec.configuration.DeviceTypeContainer;
import com.advicetec.configuration.IOSignalDeviceType;
import com.advicetec.configuration.Signal;
import com.advicetec.configuration.SignalType;
import com.advicetec.configuration.SignalUnit;
import com.advicetec.configuration.SignalUnitContainer;

public class DeviceTypeTest 
{
	@Test
	public void Test_Device_Type() 
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
		signal.setDescr("digital_signal");
		signal.setCreate_date(LocalDateTime.now());
		
		IOSignalDeviceType ioSignal = new IOSignalDeviceType(3);
		ioSignal.setI_O("asdads");
		ioSignal.setSignal(signal);

		IOSignalDeviceType ioSignal2 = new IOSignalDeviceType(4);
		ioSignal2.setI_O("jdkfdkfg");
		ioSignal2.setSignal(signal);
		
		
		DeviceType deviceType = new DeviceType(4); 
		deviceType.setCreate_date(LocalDateTime.now());
		deviceType.setDescr("Device_Type_1");
		deviceType.putIOSignal(ioSignal);
		deviceType.putIOSignal(ioSignal2);
		
		String jsonText = deviceType.toJson();
		System.out.println("Device" + jsonText);

		ConfigurationManager instance = ConfigurationManager.getInstance();
		DeviceTypeContainer dTypeContainer = instance.getDeviceTypeContainer(); 
		
		dTypeContainer.fromJSON(jsonText);
		DeviceType deviceType2 = (DeviceType) dTypeContainer.getObject(4);
		
		assertEquals("Import from Json does not work,",deviceType.toJson(), deviceType2.toJson() );

		SignalUnitContainer sUnitContainer = instance.getSignalUnitContainer();
		SignalUnit signalUnit2 =  (SignalUnit) sUnitContainer.getObject(1);
		
		assertEquals("Import from Json does not work,",signalUnit.toJson(), signalUnit2.toJson() );

		
	}
}
