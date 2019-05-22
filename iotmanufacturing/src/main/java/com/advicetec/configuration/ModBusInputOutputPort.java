package com.advicetec.configuration;

public class ModBusInputOutputPort extends InputOutputPort {

	/**
	 * Port to be connected. In the case of concentrator it is null. 
	 */
	protected int port;
	
	/**
	 * Unit id to be connected. In the case of concentrator it is null. 
	 */
	protected int unit_id;
	
	/**
	 * offset to be used. It defines from where to read or write. 
	 * In the case of concentrator it is null. 
	 */
	protected int offset;
	
	/**
	 * Number of elements to read or write.
	 * In the case of concentrator it is null. 
	 */
	protected int nbr_read;

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
		return unit_id;
	}

	/**
	 * Sets the unit id for the input-output
	 * 
	 * @param unit_id 
	 */
	public void setUnit_id(int unit_id) {
		this.unit_id = unit_id;
	}

	/**
	 * Gets the offset for the input-output
	 * 
	 * @return offset to be used
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Sets the offset for the input-output
	 * 
	 * @param offset 
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * Gets the number of elements to read 
	 * 
	 * @return nbr elements to read
	 */
	public int getNbr_read() {
		return nbr_read;
	}

	/**
	 * Sets the number of elements to read 
	 * 
	 * @param nbr_read
	 */
	public void setNbr_read(int nbr_read) {
		this.nbr_read = nbr_read;
	}
		
}
