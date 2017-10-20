package com.advicetec.eventprocessor;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.utils.ModBusUtils;

public abstract class ModBusTcpEvent extends Event
{

	static Logger logger = LogManager.getLogger(ModBusTcpEvent.class.getName());
	
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
		super(EventType.MODBUS_READ_EVENT, 
				EventType.MODBUS_READ_EVENT.getName() + "-" + 
						ipAddress + "-" + Integer.toString(port) + "-" 
						+ Integer.toString(uid));
		
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

	public static ModBusTcpEvent createModbusEvent(String ipAddress, Integer MeasuredEntityId, String portLabel, Integer refreshTimeMs)
	{
		ModBusTcpEvent retEvent = null;
		
		if (ModBusUtils.isPortLabelValid(portLabel) == false){
			logger.error("Port label" + portLabel + " is invalid");
		} else {
			
			Integer port = ModBusUtils.getPort(portLabel);
			ModBusTcpEventType type = ModBusUtils.getModBusType(portLabel);
			Integer unitId = ModBusUtils.getUnitId(portLabel);
			Integer offset = ModBusUtils.getOffset(portLabel);
			Integer count = ModBusUtils.getCount(portLabel);

			switch (type){
			case READ_DISCRETE:
				retEvent = new ModBusTcpDiscreteDataInputEvent(ipAddress, port,
						unitId, offset, count, true, refreshTimeMs);
				retEvent.setMilliseconds(refreshTimeMs); 
				break;
			case READ_REGISTER:
				retEvent = new ModBusTcpInputRegisterEvent(ipAddress, port,
						unitId, offset, count, true, refreshTimeMs);
				retEvent.setMilliseconds(refreshTimeMs); 
				break;
			case WRITE_DISCRETE:
				retEvent = new ModBusTcpDiscreteDataOutputEvent(ipAddress, port,
						unitId, offset, count, new ArrayList<Boolean>(), true, refreshTimeMs);
				retEvent.setMilliseconds(refreshTimeMs); 
				break;
			case WRITE_REGISTER:
				retEvent = new ModBusTcpReadHoldingRegisterEvent(ipAddress, port,
						unitId, offset, count, true, refreshTimeMs);
				retEvent.setMilliseconds(refreshTimeMs); 
				break;
			case READ_HOLDING_REGISTER:
				retEvent = new ModBusTcpReadHoldingRegisterEvent(ipAddress, port,
						unitId, offset, count, true, refreshTimeMs);
				retEvent.setMilliseconds(refreshTimeMs); 
				break;
			case INVALID:
				logger.error("The type of action in the port is invalid - Port label:" + portLabel);
				break;
			}
		}
		
		return retEvent;
	}
	
	@Override
	public Integer getEntity() {
		return -1;
	}
	
	@Override
	public MeasuredEntityType getOwnerType() {
		
		return MeasuredEntityType.UNDEFINED;
	}
	
}
