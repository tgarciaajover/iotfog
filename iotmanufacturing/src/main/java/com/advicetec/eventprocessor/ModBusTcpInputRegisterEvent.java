package com.advicetec.eventprocessor;

public class ModBusTcpInputRegisterEvent extends ModBusTcpEvent
{

	public ModBusTcpInputRegisterEvent(String ipAddress, int port, Integer uid) {
		super(ipAddress, port, uid, ModBusTcpEventType.READ_REGISTER);
		// TODO Auto-generated constructor stub
	}

	
}
