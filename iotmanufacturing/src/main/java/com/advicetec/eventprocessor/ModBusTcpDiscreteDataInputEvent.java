package com.advicetec.eventprocessor;

/**
 * This class represents the event designed to process a modbus tcp discrete data input. 
 * 
 * We are assuming the FOG acting as a master and requesting information from the unit. 
 * 	
 * A discrete data corresponds to digital input registers in a unit that can transmit over TCP. 
 * 
 * The following is the require information to read :
 * 		unit id	: Id of the unit sending the information
 * 		offset 	: Number of register from which it has to start reading. Offset 0 means the first digital input register, 1 the second, and so on.
 * 		count	: Number of registers to read consecutively. 
 *  
 *  For example to read registers 0, 1, and 2, it must configure offset 0, count 3.
 * 
 * @author Andres Marentes
 *
 */
public class ModBusTcpDiscreteDataInputEvent extends ModBusTcpEvent
{
	
	/**
	 *  offset of the register to bring 
	 */
	private int offset; 
	
	/**
	 * Number of registers to read
	 */
	private int count;
	
	/**
	 * Constructor of the modbus event. It receives as parameters those required to connect the unit
	 *  
	 * @param ipAddress  Listening IP address of the modbus unit
	 * @param port		 Listening Port of the modbus unit 
	 * @param uid		 Id of the unit to contact
	 * @param offset	 register offset 
	 * @param count		 Number of registers to read
	 * @param repeat	 Number of consecutive reads that should be done to get a new scan. 
	 * 					 For example if we have to read three times the sensor to get the average value. 
	 */
	public ModBusTcpDiscreteDataInputEvent(String ipAddress, int port,
			Integer uid, int offset, int count, boolean repeat, long milliseconds) {
		super(ipAddress, port, uid, ModBusTcpEventType.READ_DISCRETE);

		super.setRepeated(repeat);
		super.setMilliseconds(milliseconds);
		
		this.offset = offset;
		this.count = count;
		
	}

	/**
	 * Gets the register offset
	 * 
	 * @return  register offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Gets read count
	 * 
	 * @return read count
	 */
	public int getCount() {
		return count;
	}
	
}
