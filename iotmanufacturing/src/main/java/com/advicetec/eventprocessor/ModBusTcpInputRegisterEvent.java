package com.advicetec.eventprocessor;

public class ModBusTcpInputRegisterEvent extends ModBusTcpEvent
{

	/**
	 * offset of the register to read
	 */
	private int offset; 
	
	/**
	 * Number of register to read from the offset. 
	 */
	private int count;
		
	/**
	 * Constructor of the modbus read INPUT register event. It receives as parameters those required to connect the unit
	 * 
	 * Function FC04 in the modbus protocol. it makes offset from the 30000 register. 
	 * 
	 * @param ipAddress		Listening IP address of the modbus unit
	 * @param port			Listening Port of the modbus unit
	 * @param uid			Id of the unit to contact
	 * @param offset		register offset
	 * @param count         number of consecutive input registers to read
	 * @param repeat		it says if the event is repeated or not
	 * @param milliseconds	It says the time in milliseconds between event recurrences.
	 */
	public ModBusTcpInputRegisterEvent(String ipAddress, int port, Integer uid, int offset, int count, boolean repeat, long milliseconds) {
		super(ipAddress, port, uid, ModBusTcpEventType.READ_REGISTER);

		super.setRepeated(repeat);
		super.setMilliseconds(milliseconds);
		
		this.offset = offset;
		this.count = count;

	}

	/**
	 * Gets the register offset
	 * 
	 * @return the register offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Get the number of register to read starting in the offset.
	 * 
	 * @return the number of registers o read. 
	 */
	public int getCount() {
		return count;
	}
	
}
