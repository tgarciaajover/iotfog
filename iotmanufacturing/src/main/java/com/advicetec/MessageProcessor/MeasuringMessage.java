package com.advicetec.MessageProcessor;

import com.advicetec.configuration.InputOutputPort;
import com.advicetec.configuration.MonitoringDevice;

/**
 * This class models a Message from a Measured Entity.
 * It inherits from <code>UnifiedMessage</code>
 * 
 * @author advicetec
 * @see UnifiedMessage
 * @see UnifiedMessageType
 */
public abstract class MeasuringMessage extends UnifiedMessage {

	/**
	 * Device that is measured.
	 */
	MonitoringDevice mDevice;
	/**
	 * Port related to this message.
	 */
	InputOutputPort port;
	
	Integer mEntity;
	
	/**
	 * Regular constructor.
	 * @param type Message type
	 * @param device related monitored device
	 * @param port measured port
	 * @param entityId identifier of measured entity.
	 */
	public MeasuringMessage(UnifiedMessageType type,MonitoringDevice device, 
			InputOutputPort port, Integer entityId) {
		super(type);
		this.mDevice = device;
		this.port = port;
		mEntity = entityId;
	}
	/**
	 * Returns the monitoring device.
	 * @return the monitoring device.
	 */
	public MonitoringDevice getmDevice() {
		return mDevice;
	}
	/**
	 * Sets a monitoring device.
	 * @param mDevice
	 */
	public void setmDevice(MonitoringDevice mDevice) {
		this.mDevice = mDevice;
	}
	/**
	 * Returns the monitoring port.
	 * @return the monitoring port.
	 */
	public InputOutputPort getPort() {
		return port;
	}
	/**
	 * Sets the monitoring port.
	 * @param port monitored.
	 */
	public void setPort(InputOutputPort port) {
		this.port = port;
	}
	/**
	 * returns the measuring entity.
	 * @return the measuring entity.
	 */
	public Integer getmEntity() {
		return mEntity;
	}
	/**
	 * Sets the measuring entity.
	 * @param mEntity entity to be measured.
	 */
	public void setmEntity(Integer mEntity) {
		this.mEntity = mEntity;
	}

}
