package com.advicetec.configuration.test;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.DeviceType;
import com.advicetec.configuration.DeviceTypeContainer;
import com.advicetec.configuration.IOSignalDeviceType;
import com.advicetec.configuration.InputOutputPort;
import com.advicetec.configuration.MonitoringDevice;
import com.advicetec.configuration.MonitoringDeviceContainer;
import com.advicetec.configuration.Signal;
import com.advicetec.configuration.SignalContainer;
import com.advicetec.configuration.SignalType;
import com.advicetec.configuration.SignalUnit;
import com.advicetec.configuration.SignalUnitContainer;

public class MonitoringDeviceTest 
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
		
		InputOutputPort inputPort = new InputOutputPort(5);
		inputPort.setMeasuringEntity(2);
		inputPort.setPortLabel("COM1");
		inputPort.setSignalType(signal);
		inputPort.setTransformationText("asdakjshda");

		MonitoringDevice mDevice = new MonitoringDevice(6);
		mDevice.setDescr("Monitoring_device_1");
		mDevice.setIp_address("190.0.60.121");
		mDevice.setMac_addres("10:10:10:10");
		mDevice.setSerial("SERIE1");
		mDevice.setType(deviceType);
		mDevice.putInputOutputPort(inputPort);

		
		String jsonString = mDevice.toJson();
		System.out.println(jsonString);
		
		ConfigurationManager instance = ConfigurationManager.getInstance();
		MonitoringDeviceContainer mDeviceContainer = instance.getMonitoringDeviceContainer(); 
		
		mDeviceContainer.fromJSON(jsonString);
		MonitoringDevice mDevice2 = (MonitoringDevice) mDeviceContainer.getObject(6);
		
		assertEquals("Import from Json does not work,",mDevice.toJson(), mDevice2.toJson() );

		SignalUnitContainer sUnitContainer = instance.getSignalUnitContainer();
		SignalUnit signalUnit2 =  (SignalUnit) sUnitContainer.getObject(1);
		
		assertEquals("Import from Json does not work,",signalUnit.toJson(), signalUnit2.toJson() );

	}
}
