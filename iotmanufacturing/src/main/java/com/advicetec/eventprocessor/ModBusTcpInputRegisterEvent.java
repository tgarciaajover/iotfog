package com.advicetec.eventprocessor;

public class ModBusTcpInputRegisterEvent extends ModBusTcpEvent
{

	// offset of the register to bring 
	private int offset; 
	
	// Number of id to read
	private int count;
		
	public ModBusTcpInputRegisterEvent(String ipAddress, int port, Integer uid, int offset, int count, boolean repeat) {
		super(ipAddress, port, uid, ModBusTcpEventType.READ_REGISTER);

		super.setRepeated(repeat);
		this.offset = offset;
		this.count = count;

	}

	public int getOffset() {
		return offset;
	}

	public int getCount() {
		return count;
	}
	
}
