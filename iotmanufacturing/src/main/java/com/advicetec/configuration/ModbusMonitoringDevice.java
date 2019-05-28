package com.advicetec.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.advicetec.eventprocessor.ModBusTcpEvent;
import com.advicetec.eventprocessor.ModBusTcpEventType;

public class ModbusMonitoringDevice extends MonitoringDevice {

	
	/**
	 * This property tells whether or not the monitoring device is a concentrator
	 */
	protected boolean isConcentrator;
	
	/**
	 * This property establishes the port to be connected
	 */
	protected int port;
	
	/**
	 * This property establishes the unit to be connected
	 */
	protected int unitId;
	
	/**
	 * This property establishes the positions from where to start reading or writing
	 */
	protected int offset;
	
	/**
	 * This property establishes the number of registers (coils) to read or write
	 */
	protected int nbrRead;
	
	/**
	 * This property establishes the type of object (Modbus: coil, discrete unit, 
	 * 						input register, holding register) 
	 */
	protected ModbusObjectType objectType;
	
	/**
	 * This property establishes the action to be done (read, write, read/write).
	 */
	protected ModbusAccess access;
		
	/**
	 * This property establishes the refresh time for the unit.
	 */
	protected int refreshTime;

	
	/**
	 * Constructor for the class. It receives the identifier of the measuring entity.
	 * @param id Identifier of the measuring entity.
	 */
	@JsonCreator
	public ModbusMonitoringDevice(@JsonProperty("id") Integer id) {
		super(id);
	}
	
	/**
	 * Returns if the monitoring device is or not a concentrator
	 * @return
	 */
	public boolean isConcentrator() {
		return isConcentrator;
	}

	/**
	 * Sets the monitoring device as a concentrator
	 * 
	 * @param isConcentrator
	 */
	public void setConcentrator(boolean isConcentrator) {
		this.isConcentrator = isConcentrator;
	}

	/**
	 * Gets the port for the monitoring device. This value is used when the monitoring device is a
	 * concentrator.
	 * 
	 * @return
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port for the monitoring device. This value is used when the monitoring device is a
	 * concentrator.
	 * 
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Gets the modbus unit id used by monitoring the device. This value is used when the monitoring device is a
	 * concentrator.
	 * 
	 * @return
	 */
	public int getUnit_id() {
		return unitId;
	}

	/**
	 * Sets the modbus unit id used by monitoring the device. This value is used when the monitoring device is a
	 * concentrator.
	 * 
	 * @param unit_id
	 */
	public void setUnit_id(int unit_id) {
		this.unitId = unit_id;
	}

	/**
	 * Gets the offset used by monitoring the device. This value is used when the monitoring device is a
	 * concentrator.
	 * 
	 * @return
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Sets the offset used by monitoring the device. This value is used when the monitoring device is a
	 * concentrator.
	 * 
	 * @param offset
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * Gets the number of objects types to read/write by monitoring the device. 
	 * This value is used when the monitoring device is a concentrator.
	 * 
	 * @return
	 */
	public int getNbr_read() {
		return nbrRead;
	}

	/**
	 * Sets the number of objects types to read/write by monitoring the device. 
	 * This value is used when the monitoring device is a concentrator.
	 * 
	 * @param nbr_read
	 */
	public void setNbr_read(int nbr_read) {
		this.nbrRead = nbr_read;
	}

	/**
	 * Gets the objects types to read/write by monitoring the device. 
	 * This value is used when the monitoring device is a concentrator.
	 *
	 * @return
	 */
	public ModbusObjectType getObject_type() {
		return objectType;
	}

	/**
	 * Sets the objects types to read/write by monitoring the device. 
	 * This value is used when the monitoring device is a concentrator.
	 *
	 * @param object_type
	 */
	public void setObject_type(ModbusObjectType object_type) {
		this.objectType = object_type;
	}

	/**
	 * Gets the type of access (read/write) to be used by monitoring the device. 
	 * This value is used when the monitoring device is a concentrator.
	 *
	 * @return
	 */
	public ModbusAccess getAccess() {
		return access;
	}

	/**
	 * Sets the type of access (read/write) to be used by monitoring the device. 
	 * This value is used when the monitoring device is a concentrator.
	 * 
	 * @param access
	 */
	public void setAccess(ModbusAccess access) {
		this.access = access;
	}
	
	
	/**
	 * Gets the input output port by identifier
	 * @param id identifier of the input - output port.
	 * @return input output port object or null if there is not a port with that identifier.
	 */
	@JsonIgnore
	public ModbusInputOutputPort getInputOutputPort(Integer id){

		for (int i = 0; i < this.inputOutputPorts.size(); i++){
			if (this.inputOutputPorts.get(i).getId().equals(id)){
				return (ModbusInputOutputPort) this.inputOutputPorts.get(i);
			}
		}
		
		return null;
		
	}
	
	/**
	 * Gets the Modbus events that are required to schedule for this measuring device
	 * 
	 * @return List fo modbus events.
	 */
	public List<ModBusTcpEvent> getModbusEvents(){
		
		List<ModBusTcpEvent> events = new ArrayList<ModBusTcpEvent>();
		
		if (this.isConcentrator) {
			
			ModBusTcpEvent modBusEvent = ModBusTcpEvent.createModbusEvent(   			
					this.isConcentrator(), null, this.getIp_address(), 
					this.getPort(), this.getRefreshTime(), this.getObject_type(), this.getAccess(), 
					this.getUnit_id(), this.getOffset(), this.getNbr_read());
			
		} else {
		
			String ipAddress = this.getIp_address();
			
			for (int i = 0; i < this.inputOutputPorts.size(); i++){
				ModbusInputOutputPort inputOutputPort = (ModbusInputOutputPort) this.inputOutputPorts.get(i);
				logger.debug("Type of protocol of the port signal:" + inputOutputPort.getSignalType().getType().getProtocol() );
				if (inputOutputPort.getSignalType().getType().getProtocol().equals("M")) {
					
					int port = inputOutputPort.getPort();
					int offset = inputOutputPort.getOffset();
					int unit_id = inputOutputPort.getUnit_id();
					int nbrRead = inputOutputPort.getNbr_read();
					String portLabel = inputOutputPort.getPortLabel();
					ModbusObjectType objectType = inputOutputPort.getObjectType();
					ModbusAccess access = inputOutputPort.getAccess();
					
					Integer refreshTimeMs = inputOutputPort.getRefreshTimeMs();
	
					if (refreshTimeMs.compareTo(0) > 0){
						ModBusTcpEvent modBusEvent = ModBusTcpEvent.createModbusEvent(
								this.isConcentrator(), inputOutputPort, ipAddress, port, refreshTimeMs, 
								objectType, access, unit_id, offset, nbrRead);
						if (modBusEvent != null)
							events.add(modBusEvent);
					} else {
						logger.error("Refresh time is zero for Port label:" + portLabel + " which is invalid");
					}
				}
			}
		}
		
		return events;
		
	}
	
	/**
	 * Gets the refresh time (how often should read new data)
	 * @return
	 */
	public int getRefreshTime() {
		return this.refreshTime;
	}

	/**
	 * Sets the refresh time (how often should read new data)
	 * @param refreshTime
	 */
	public void setRefreshTime(int refreshTime) {
		this.refreshTime = refreshTime;
	}

	
}
