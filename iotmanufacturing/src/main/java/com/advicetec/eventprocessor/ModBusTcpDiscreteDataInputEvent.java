package com.advicetec.eventprocessor;

public class ModBusTcpDiscreteDataInputEvent extends ModBusTcpEvent
{

	// Discrete Data input
	
	// offset of the register to bring 
	private int offset; 
	
	// Number of id to read
	private int count;
	
	// Number of times to repeat the read.
	private int repeat;

	public ModBusTcpDiscreteDataInputEvent(String ipAddress, int port,
			Integer uid, int offset, int count, int repeat) {
		super(ipAddress, port, uid, ModBusTcpEventType.READ_DISCRETE);
		// TODO Auto-generated constructor stub
		
		this.offset = offset;
		this.count = count;
		this.repeat = repeat;
		
	}

	public int getOffset() {
		return offset;
	}

	public int getCount() {
		return count;
	}

	public int getRepeat() {
		return repeat;
	}
	
}
