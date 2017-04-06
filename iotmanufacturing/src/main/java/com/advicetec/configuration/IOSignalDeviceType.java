package com.advicetec.configuration;

public class IOSignalDeviceType extends ConfigurationObject
{

	private Signal signal;
    private String I_O;
    
	public IOSignalDeviceType(Integer id) {
		super(id);
	}
	
	public Signal getSignal() {
		return signal;
	}
	public void setSignal(Signal signal) {
		this.signal = signal;
	}
	public String getI_O() {
		return I_O;
	}
	public void setI_O(String i_O) {
		I_O = i_O;
	}
    
    
	
}
