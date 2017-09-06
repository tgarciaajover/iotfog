package com.advicetec.eventprocessor;

/**
 * This class represents the event designed to process a modbus tcp Read Holding data input. 
 * 
 * We are assuming the FOG acting as a master and requesting information from the unit. 
 * 	
 * A Read Holding data corresponds to a Holding input register in a unit that can transmit over TCP. 
 * 
 * The following is the required information to read :
 * 
 * 		unit id	: Id of the unit sending the information
 * 		offset 	: Number of register from which it has to start reading. Offset 0 means the first Holding input/output register, 1 the second, and so on.
 * 		count	: Number of registers to read consecutively. 
 *  
 *  For example to read registers 0, 1, and 2, it must configure offset 0, count 3.
 * 
 * @author Andres Marentes
 *
 */
public class ModBusTcpReadHoldingRegisterEvent extends ModBusTcpEvent
{

	/**
	 * offset of the register to read
	 */
	private int offset; 
	
	/**
	 * Number of registers to read
	 */
	private int count;
		
	/**
	 * Constructor of the modbus holding register event. It receives as parameters those required to connect the unit
	 * 
	 * Function FC03 in the modbus protocol. it makes offset from the 40000 register. 
	 * 
	 * @param ipAddress 	Listening IP address of the modbus unit
	 * @param port			Listening Port of the modbus unit
	 * @param uid			Id of the unit to contact
	 * @param offset		register offset 
	 * @param count			Number of registers to read
	 * @param repeat		Number of times that it should write the data.
	 * @param milliseconds  It says the time in milliseconds between event recurrences.
	 */
	public ModBusTcpReadHoldingRegisterEvent(String ipAddress, int port, Integer uid, int offset, int count, boolean repeat, long milliseconds) {
		super(ipAddress, port, uid, ModBusTcpEventType.READ_HOLDING_REGISTER);

		super.setRepeated(repeat);
		super.setMilliseconds(milliseconds);
		
		this.offset = offset;
		this.count = count;

	}

	/**
	 * Gets the register offset
	 * @return register offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Gets the register count
	 * 
	 * @return register count
	 */
	public int getCount() {
		return count;
	}
	
}
