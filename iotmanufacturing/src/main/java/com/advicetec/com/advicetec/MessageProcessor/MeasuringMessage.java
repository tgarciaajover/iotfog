package com.advicetec.MessageProcessor;

import com.advicetec.FogClasses.MeasuredEntity;
import com.advicetec.configuration.InputOutputPort;
import com.advicetec.configuration.MonitoringDevice;

public abstract class MeasuringMessage extends UnifiedMessage {

	MonitoringDevice mDevice;
	InputOutputPort port;
	MeasuredEntity mEntity;
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
	public MeasuredEntity getmEntity() {
		return mEntity;
	}
	public void setmEntity(MeasuredEntity mEntity) {
		this.mEntity = mEntity;
	}

}
