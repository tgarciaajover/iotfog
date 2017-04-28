package com.advicetec.MessageProcessor;

import com.advicetec.configuration.InputOutputPort;
import com.advicetec.configuration.MonitoringDevice;
import com.advicetec.measuredentitity.MeasuredEntity;

public abstract class MeasuringMessage extends UnifiedMessage {

	MonitoringDevice mDevice;
	InputOutputPort port;
	Integer mEntity;
	
	
	public MeasuringMessage(UnifiedMessageType type,MonitoringDevice device, InputOutputPort port, Integer entityId) {
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
	public Integer getmEntity() {
		return mEntity;
	}
	public void setmEntity(Integer mEntity) {
		this.mEntity = mEntity;
	}

}
