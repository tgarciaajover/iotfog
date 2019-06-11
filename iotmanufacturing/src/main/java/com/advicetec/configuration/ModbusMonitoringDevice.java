package com.advicetec.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.core.serialization.BooleanDeserializer;
import com.advicetec.core.serialization.BooleanSerializer;
import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;
import com.advicetec.core.serialization.ModbusAccessDeserializer;
import com.advicetec.core.serialization.ModbusAccessSerializer;
import com.advicetec.core.serialization.ModbusObjectTypeDeserializer;
import com.advicetec.core.serialization.ModbusObjectTypeSerializer;
import com.advicetec.eventprocessor.ModBusTcpEvent;
import com.advicetec.eventprocessor.ModBusTcpEventType;

public class ModbusMonitoringDevice extends MonitoringDevice {

	
	/**
	 * This property tells whether or not the monitoring device is a concentrator
	 */
	@JsonProperty("is_concentrator")
	@JsonSerialize(using = BooleanSerializer.class)
	@JsonDeserialize(using = BooleanDeserializer.class)		
	protected boolean isConcentrator;
	
	/**
	 * This property establishes the port to be connected
	 */
	@JsonProperty("port")
	protected int port;
	
	/**
	 * This property establishes the unit to be connected
	 */
	@JsonProperty("unit_id")
	protected int unitId;
	
	/**
	 * This property establishes the positions from where to start reading or writing
	 */
	@JsonProperty("offset")	
	protected int offset;
	
	/**
	 * This property establishes the number of registers (coils) to read or write
	 */
	@JsonProperty("nbr_read")	
	protected int nbrRead;
	
	/**
	 * This property establishes the type of object (Modbus: coil, discrete unit, 
	 * 						input register, holding register) 
	 */
	@JsonProperty("object_type") 
	@JsonSerialize(using = ModbusObjectTypeSerializer.class)
	@JsonDeserialize(using = ModbusObjectTypeDeserializer.class)		
	protected ModbusObjectType objectType;
	
	/**
	 * This property establishes the action to be done (read, write, read/write).
	 */
	@JsonProperty("access") 
	@JsonSerialize(using = ModbusAccessSerializer.class)
	@JsonDeserialize(using = ModbusAccessDeserializer.class)		
	protected ModbusAccess access;
		
	/**
	 * This property establishes the refresh time for the unit.
	 */
	@JsonProperty("refresh_time_ms")		
	protected int refreshTime;

	/**
	 * List of ports included in the measuring device.
	 */
	@JsonProperty("modbus_io_ports")
	protected List<ModbusInputOutputPort> inputOutputPorts;
	
	/**
	 * Constructor for the class. It receives the identifier of the measuring entity.
	 * @param id Identifier of the measuring entity.
	 */
	@JsonCreator
	public ModbusMonitoringDevice(@JsonProperty("id") Integer id) {
		super(id);
		inputOutputPorts = new ArrayList<ModbusInputOutputPort>();
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
		
		logger.debug("Reading Modbus InputOutPut Port id:" + id.toString() + " Num Ports:" + this.inputOutputPorts.size() );
		for (int i = 0; i < this.inputOutputPorts.size(); i++){
			if (this.inputOutputPorts.get(i).getId().equals(id)){
				return (ModbusInputOutputPort) this.inputOutputPorts.get(i);
			}
		}
		
		return null;
	}
	
	/**
	 * Adds a port into the list of input output ports.
	 * 
	 * @param iop Input - Output port object to add.
	 */
	@JsonIgnore
	public void putInputOutputPort(InputOutputPort iop){
		logger.debug("port label:" + iop.getPortLabel() + "Id:" + iop.getId());
		this.inputOutputPorts.add((ModbusInputOutputPort)iop);
	}
	
	/**
	 * Returns all input-output ports within the measured device
	 * 
	 */
	@JsonIgnore
	public List<ModbusInputOutputPort> getInputOutputPorts(){		
		return this.inputOutputPorts;
	}
	
	@JsonIgnore
	public List<InputOutputPort> getInputOutputPortReferingMeasuredEntity(Integer measuredEntity){
		
		List<InputOutputPort> ports = new ArrayList<InputOutputPort>();
		
		for (int i = 0; i < this.inputOutputPorts.size(); i++){
			InputOutputPort inputOutputPort = this.inputOutputPorts.get(i);
			if (inputOutputPort.getMeasuringEntity().equals(measuredEntity)) {
				ports.add(inputOutputPort);
			}
		}
		
		return ports;
	}
	
	/**
	 * Gets the Modbus events that are required to schedule for this measuring device
	 * 
	 * @return List fo modbus events.
	 */
	@JsonIgnore
	public List<ModBusTcpEvent> getModbusEvents() {
		
		List<ModBusTcpEvent> events = new ArrayList<ModBusTcpEvent>();
		
		if (this.isConcentrator) {
			
			ModBusTcpEvent modBusEvent = ModBusTcpEvent.createModbusEvent(   			
					this.isConcentrator(), null, this.getIp_address(), 
					this.getPort(), this.getRefreshTime(), this.getObject_type(), this.getAccess(), 
					this.getUnit_id(), this.getOffset(), this.getNbr_read());
			
			if (modBusEvent != null)
				events.add(modBusEvent);
			
		} else {
		
			String ipAddress = this.getIp_address();
			
			for (int i = 0; i < this.inputOutputPorts.size(); i++){
				ModbusInputOutputPort inputOutputPort = (ModbusInputOutputPort) this.inputOutputPorts.get(i);
				logger.debug("Type of protocol of the port signal:" + inputOutputPort.getSignalType().getType().getProtocol() );
				if (inputOutputPort.getSignalType().getType().getProtocol().equals("M")) {
					
					int port = inputOutputPort.getPort();
					int offset = inputOutputPort.getOffset();
					int unitId = inputOutputPort.getUnit_id();
					int nbrRead = inputOutputPort.getNbr_read();
					String portLabel = inputOutputPort.getPortLabel();
					ModbusObjectType objectType = inputOutputPort.getObjectType();
					ModbusAccess access = inputOutputPort.getAccess();
					
					Integer refreshTimeMs = inputOutputPort.getRefreshTimeMs();
	
					if (refreshTimeMs.compareTo(0) > 0){
						ModBusTcpEvent modBusEvent = ModBusTcpEvent.createModbusEvent(
								this.isConcentrator(), inputOutputPort, ipAddress, port, refreshTimeMs, 
								objectType, access, unitId, offset, nbrRead);
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
