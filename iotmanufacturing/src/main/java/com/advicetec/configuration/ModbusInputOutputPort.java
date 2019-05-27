package com.advicetec.configuration;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class ModbusInputOutputPort extends InputOutputPort {

	/**
	 * Port to be connected. In the case of concentrator it is null. 
	 */
	@JsonProperty("port")
	protected int port;
	
	/**
	 * Unit id to be connected. In the case of concentrator it is null. 
	 */
	@JsonProperty("unit_id")
	protected int unitId;
	
	/**
	 * offset to be used. It defines from where to read or write. 
	 * In the case of concentrator it is null. 
	 */
	@JsonProperty("offset")
	protected int offSet;
	
	/**
	 * Number of elements to read or write.
	 * In the case of concentrator it is null. 
	 */
	@JsonProperty("nbr_read")
	protected int nbrRead;
	
	/**
	 * Type of registers to be read or written.
	 * In the case of concentrator it is null. 
	 */	
	@JsonProperty("object_type")
	protected ModbusObjectType objectType;

	/**
	 * Access mode for modbus (read, write, read/write)
	 * In the case of concentrator it is null. 
	 */	
	@JsonProperty("access")
	protected ModbusAccess access;
	
	/**
	 * Constructor for a ModBusInputOutputPort
	 * @param id  identifier of this configurable object.
	 */
	@JsonCreator
	public ModbusInputOutputPort(@JsonProperty("id") Integer id) {
		super(id);
	}

	/**
	 * Gets the port for the input-output
	 * 
	 * @return port number 
	 */
	public int getPort() {
		return port;
	}

	
	/**
	 * Sets the port for the input-output
	 * 
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Gets the unit id for the input-output
	 * 
	 * @return unit id
	 */
	public int getUnit_id() {
		return unitId;
	}

	/**
	 * Sets the unit id for the input-output
	 * 
	 * @param unit_id 
	 */
	public void setUnit_id(int unit_id) {
		this.unitId = unit_id;
	}

	/**
	 * Gets the offset for the input-output
	 * 
	 * @return offset to be used
	 */
	public int getOffset() {
		return offSet;
	}

	/**
	 * Sets the offset for the input-output
	 * 
	 * @param offset 
	 */
	public void setOffset(int offset) {
		this.offSet = offset;
	}

	/**
	 * Gets the number of elements to read 
	 * 
	 * @return nbr elements to read
	 */
	public int getNbr_read() {
		return nbrRead;
	}

	/**
	 * Sets the number of elements to read 
	 * 
	 * @param nbr_read
	 */
	public void setNbr_read(int nbr_read) {
		this.nbrRead = nbr_read;
	}

	/**
	 * Gets the access mode
	 * 
	 * @return access to be performed  
	 */	
	public ModbusAccess getAccess() {
		return access;
	}

	/**
	 * Sets the access mode
	 * 
	 * @param access to be performed  
	 */	
	public void setAccess(ModbusAccess access) {
		this.access = access;
	}

	/**
	 * Gets the object type to be read, write, read/write
	 * 
	 * @return access to be performed  
	 */	
	public ModbusObjectType getObjectType() {
		return objectType;
	}

	/**
	 * Sets the object type to be read, write, read/write
	 * 
	 * @return access to be performed  
	 */	
	public void setObjectType(ModbusObjectType objectType) {
		this.objectType = objectType;
	}
	
}
