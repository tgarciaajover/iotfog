package com.advicetec.configuration.test;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.DeviceType;
import com.advicetec.configuration.DeviceTypeContainer;
import com.advicetec.configuration.IOSignalDeviceType;
import com.advicetec.configuration.InputOutputPort;
import com.advicetec.configuration.Signal;
import com.advicetec.configuration.SignalType;
import com.advicetec.configuration.SignalUnit;
import com.advicetec.configuration.SignalUnitContainer;

public class InputOutputPortTest 
{
	@Test
	public void Test_Input_Output_Port() 
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
		
		InputOutputPort inputPort = new InputOutputPort(3);
		inputPort.setMeasuringEntity(2);
		inputPort.setPortLabel("COM1");
		inputPort.setSignalType(signal);
		inputPort.setTransformationText("asdakjshda");
		
		String jsonText = inputPort.toJson();
		System.out.println("Input Output Port " + jsonText);

		
	}
}
