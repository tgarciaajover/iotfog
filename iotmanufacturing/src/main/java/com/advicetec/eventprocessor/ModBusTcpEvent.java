package com.advicetec.eventprocessor;

public abstract class ModBusTcpEvent extends Event
{

	// Type of modbus type
	private ModBusTcpEventType type;
		
	// Ip Address of the Modbus Tcp Slave
	private String ipAddress;
	
	// Port where the Modbus Tcp Slave is listening
	private int port;
	
	// Slave UId  
	private Integer Uid;
	
	// Milliseconds when it has to be repeated
	private long milliseconds; 
	
	// it says if it has to be repeated or not.
	private boolean repeated;
	
	public ModBusTcpEvent(String ipAddress, int port, Integer uid, ModBusTcpEventType type) {
		super(EventType.MODBUS_READ_EVENT);
		this.ipAddress = ipAddress;
		this.port = port;
		this.Uid = uid;
		this.type = type;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Integer getUid() {
		return Uid;
	}

	public void setUid(Integer uid) {
		Uid = uid;
	}

	public ModBusTcpEventType getType() {
		return type;
	}

	public String getKey(){
		return getEvntType().getName() + "-" + getIpAddress() + "-" + getPort() + "-" + getUid();
	}

}
