package com.advicetec.eventprocessor;

import java.util.List;

/**
 * This class represents the event designed to process a modbus tcp discrete data output (write). 
 * 	
 * We are assuming the FOG acting as a master and writing information in the unit. 
 * 
 * A discrete data output corresponds to digital output register in a unit that can transmit over TCP. 
 * 
 * The following is the require information to write
 * 
 * 		unit id	: Id of the unit sending the information
 * 		offset 	: Number of register from which it has to start reading. Offset 0 means the first digital input register, 1 the second, and so on.
 * 		value 	: value to be write in the register. 
 *  
 * @author Andres Marentes
 *
 */public class ModBusTcpDiscreteDataOutputEvent extends ModBusTcpEvent
{

	/**
	 * Offset of the register to be written
	 */
	private int offset; 
	
	/**
	 * number of registers to write from the offset.
	 */
	private int count;
	
	/**
	 * List of booleans which are the values to write in the registers. This list should be at least as long as count.
	 */
	private List<Boolean> values; 
	
	/**
	 * Constructor of the modbus digital write event. It receives as parameters those required to connect the unit
	 * 
	 * Function FC02 in the modbus protocol. it makes offset from the 10000 register. 
	 * 
	 * @param ipAddress 	Listening IP address of the modbus unit
	 * @param port			Listening Port of the modbus unit
	 * @param uid			Id of the unit to contact
	 * @param offset		register offset 
	 * @param count			Number of registers to read
	 * @param repeat		Number of times that it should write the data.
	 */
	public ModBusTcpDiscreteDataOutputEvent(String ipAddress, int port,
			Integer uid, int offset, int count, List<Boolean> values, boolean repeat, long milliseconds) {
		super(ipAddress, port, uid, ModBusTcpEventType.WRITE_DISCRETE);

		super.setRepeated(repeat);
		super.setMilliseconds(milliseconds);
		this.offset = offset;
		this.count = count;
		this.values = values;
	}

	/**
	 * Gets the Offset register
	 * 
	 * @return Offset register
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Gets the register count
	 * @return register count
	 */
	public int getCount() {
		return count;
	}
	
	/**
	 * Gets values to write in registers.
	 * 
	 * @return list of values to write.
	 */
	public List<Boolean> getValues(){
		return this.values;
	}
	
}
