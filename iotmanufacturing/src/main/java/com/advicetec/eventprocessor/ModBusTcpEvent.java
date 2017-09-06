package com.advicetec.eventprocessor;

public abstract class ModBusTcpEvent extends Event
{

	/**
	 * Type of modbus event
	 */
	private ModBusTcpEventType type;
		
	/**
	 *  Ip Address of the Modbus Tcp Slave
	 */
	private String ipAddress;
	
	/**
	 * Port where the Modbus Tcp Slave is listening
	 */
	private int port;
	
	/**
	 * Slave Unit Id within the modbus slave.
	 */
	private Integer Uid;
		
	/**
	 * Constructor for the class, specifies the event as modbus read event
	 * @param ipAddress  Listening IP address of the modbus unit
	 * @param port		 Listening Port of the modbus unit
	 * @param uid		 Id of the unit to contact
	 * @param type		 Type of read to execute. Valid types are defined in the enumeration ModBusTcpEventType. 
	 */
	public ModBusTcpEvent(String ipAddress, int port, Integer uid, ModBusTcpEventType type) {
		super(EventType.MODBUS_READ_EVENT);
		this.ipAddress = ipAddress;
		this.port = port;
		this.Uid = uid;
		this.type = type;
	}

	/**
	 * Gets the listening IP Address of the modbus Slave
	 * @return IP address 
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * Sets the listening IP Address of the modbus Slave
	 * 
	 * @param ipAddress String representation of the Ip Address
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * Gets the listening port of modbus slave  
	 * 
	 * @return listening port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the listening port of modbus slave
	 * 
	 * @param port modbus slave listening port 
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Gets the unit id within the modbus slave 
	 * 
	 * @return Unit id.
	 */
	public Integer getUid() {
		return Uid;
	}

	/**
	 * Sets the unit id within the modbus slave
	 * 
	 * @param uid  unit Id.
	 */
	public void setUid(Integer uid) {
		Uid = uid;
	}

	/**
	 * Gets the type of read 
	 * 
	 * @return read modbus type
	 */
	public ModBusTcpEventType getType() {
		return type;
	}

	/**
	 * Obtains a key to identify this modbus event.
	 * The key in this case is the concatenation of the following fields:
	 * 
	 * 		event type
	 * 		modbus event type
	 * 		ip address
	 * 		port
	 * 		unit id
	 */
	public String getKey(){
		return getEvntType().getName() + "-" + getIpAddress() + "-" + getPort() + "-" + getUid();
	}

}
