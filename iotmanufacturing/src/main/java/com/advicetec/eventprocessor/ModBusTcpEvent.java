package com.advicetec.eventprocessor;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.configuration.ModbusAccess;
import com.advicetec.configuration.ModbusInputOutputPort;
import com.advicetec.configuration.ModbusObjectType;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.utils.ModBusUtils;

public abstract class ModBusTcpEvent extends Event {

	static Logger logger = LogManager.getLogger(ModBusTcpEvent.class.getName());

	/**
	 * Type of modbus event
	 */
	private ModBusTcpEventType type;

	/**
	 * Ip Address of the Modbus Tcp Slave
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
	 * Specifies whether the slave acts as a concentrator.
	 */
	private boolean concentrator;
	
	/**
	 * Indicate the input output port whenever is not a concentrator.
	 */
	private ModbusInputOutputPort inputOutputPort;


	/**
	 * Constructor for the class, specifies the event as modbus read event
	 * 
	 * @param ipAddress    		Listening IP address of the modbus unit
	 * @param port         		Listening Port of the modbus unit
	 * @param uid          		Id of the unit to contact
	 * @param type         		Type of read to execute. Valid types are defined in the
	 *                     		enumeration ModBusTcpEventType.
	 * @param concentrator 		Specifies whether or not the remote slave is a
	 *                     		concentrator
	 * @param inputOutputPort 	
	 */
	public ModBusTcpEvent(boolean concentrator, ModbusInputOutputPort inputOutputPort, String ipAddress, 
							int port, Integer uid, ModBusTcpEventType type) {
		
		super(EventType.MODBUS_READ_EVENT, EventType.MODBUS_READ_EVENT.getName() + "-" + Boolean.toString(concentrator) + "-" + ipAddress + "-"
				+ Integer.toString(port) + "-" + Integer.toString(uid));

		this.ipAddress = ipAddress;
		this.port = port;
		this.Uid = uid;
		this.type = type;
		this.concentrator = concentrator;
		this.inputOutputPort = inputOutputPort;
	}

	/**
	 * Gets the listening IP Address of the modbus Slave
	 * 
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
	 * @param uid unit Id.
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

	public static ModBusTcpEvent createModbusEvent(boolean isConcentrator, ModbusInputOutputPort inputOutputPort, 
			String ipAddress, int port, Integer refreshTimeMs, ModbusObjectType type, 
			ModbusAccess access, int unitId, int offset, int count) {
		ModBusTcpEvent retEvent = null;

		switch (type) {
		
		case COIL:
			if (access == ModbusAccess.READ) {
				/* TODO Not implemented yet */
				logger.error("The type of modbus event coil - read is not implemented" );
			} else if (access == ModbusAccess.WRITE) {
				/* TODO Not implemented yet */
				logger.error("The type of modbus event coil - write is not implemented" );
			} else if (access == ModbusAccess.READ_WRITE) {
				/* TODO Not implemented yet */
				logger.error("The type of modbus event coil - read/write is not implemented" );
			} else {
				logger.error("The type of modbus access is invalid :" + access.name);
				break;
			}
			
		case DISCRETE:
			if (access == ModbusAccess.READ) {
				retEvent = new ModBusTcpDiscreteDataInputEvent(isConcentrator, inputOutputPort, ipAddress, port, unitId, 
																offset, count, true, refreshTimeMs);
				retEvent.setMilliseconds(refreshTimeMs);
				break;
			} else if (access == ModbusAccess.WRITE) {
				retEvent = new ModBusTcpDiscreteDataOutputEvent(isConcentrator, inputOutputPort, ipAddress, port, unitId, offset, count,
															new ArrayList<Boolean>(), true, refreshTimeMs);
				retEvent.setMilliseconds(refreshTimeMs);
				break;
			} else if (access == ModbusAccess.READ_WRITE) {
				/* TODO Not implemented yet */
				logger.error("The type of modbus event discrite - read/write is not implemented" );
			} else {
				logger.error("The type of modbus access is invalid :" + access.name);
				break;
			}

		case REGISTER:
			if (access == ModbusAccess.READ) {
				retEvent = new ModBusTcpInputRegisterEvent(isConcentrator, inputOutputPort, ipAddress, port, unitId, offset, count, true, refreshTimeMs);
				retEvent.setMilliseconds(refreshTimeMs);
				break;
			} else if (access == ModbusAccess.WRITE) {
				retEvent = new ModBusTcpReadHoldingRegisterEvent(isConcentrator, inputOutputPort, ipAddress, port, unitId, offset, count, true,
						refreshTimeMs);
				retEvent.setMilliseconds(refreshTimeMs);
				break;
			} else if (access == ModbusAccess.READ_WRITE) {
				logger.error("The type of modbus event register - read/write is not implemented" );
			} else {
				if (access != null) {
					logger.error("The type of modbus access is invalid :" + access);
				} else {
					logger.error("The type of modbus access is null");
				}
				break;
			}
			
		case HOLDING_REGISTER:
			if (access == ModbusAccess.READ) {
				retEvent = new ModBusTcpReadHoldingRegisterEvent(isConcentrator, inputOutputPort, ipAddress, port, unitId, offset, count, true,
						refreshTimeMs);
				retEvent.setMilliseconds(refreshTimeMs);
				break;
			} else if (access == ModbusAccess.WRITE) {
				logger.error("The type of modbus event holding register - write is not implemented" );
			} else if (access == ModbusAccess.READ_WRITE) {
				logger.error("The type of modbus event holding register - read/write is not implemented" );
			} else {
				if (access != null) {
					logger.error("The type of modbus access is invalid :" + access);
				} else {
					logger.error("The type of modbus access is null");
				}
				break;
			}
			
		case INVALID:
			logger.error("The type of modbus object type is invalid :" + type.name);
			break;
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

	/**
	 * Gets whether or not the event was created for a concentrator monitoring device.
	 * @return
	 */
	public boolean getIsConcentrator() {
		return this.concentrator;
	}

	/**
	 * Gets the input output port that triggers the event when is not a concentrator monitoring device
	 * 
	 * @return
	 */
	public ModbusInputOutputPort getInputOutputPort() {
		return inputOutputPort;
	}

	/**
	 * Sets the input output port that triggers the event when is not a concentrator monitoring device
	 * 
	 * @param inputOutputPort
	 */
	public void setInputOutputPort(ModbusInputOutputPort inputOutputPort) {
		this.inputOutputPort = inputOutputPort;
	}
	
	

}
