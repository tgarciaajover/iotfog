package com.advicetec.MessageProcessor;

import com.advicetec.configuration.InputOutputPort;
import com.advicetec.configuration.MonitoringDevice;
import com.advicetec.measuredentitity.MeasuredEntity;

public abstract class MeasuringMessage extends UnifiedMessage {

	MonitoringDevice mDevice;
	InputOutputPort port;
	String mEntity;
	
	
	public MeasuringMessage(UnifiedMessageType type,MonitoringDevice device, InputOutputPort port, String entityId) {
		super(type);
		mDevice = mDevice;
		port = port;
		mEntity = entityId;
	}
	
	public MonitoringDevice getmDevice() {
		return mDevice;
	}
	public void setmDevice(MonitoringDevice mDevice) {
		this.mDevice = mDevice;
	}
	public InputOutputPort getPort() {
		return port;
	}
	public void setPort(InputOutputPort port) {
		this.port = port;
	}
	public String getmEntity() {
		return mEntity;
	}
	public void setmEntity(String mEntity) {
		this.mEntity = mEntity;
	}

}
