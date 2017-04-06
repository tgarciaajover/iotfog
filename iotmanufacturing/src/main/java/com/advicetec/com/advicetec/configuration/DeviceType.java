package com.advicetec.configuration;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class DeviceType extends ConfigurationObject
{
	private String descr;
    private LocalDateTime create_date;
    private Map<Integer, IOSignalDeviceType> signals;
    
	public DeviceType(Integer id) {
		super(id);
		signals = new HashMap<Integer, IOSignalDeviceType>();
	}
	
	public String getDescr() {
		return descr;
	}
	
	public void setDescr(String descr) {
		this.descr = descr;
	}
	
	public LocalDateTime getCreate_date() {
		return create_date;
	}
	public void setCreate_date(LocalDateTime create_date) {
		this.create_date = create_date;
	}  
    
	public void putIOSignal(IOSignalDeviceType signal)
	{
		this.signals.put(signal.getId(), signal);
	}
	
	public IOSignalDeviceType getIOSignal(Integer signalId)
	{
		return this.signals.get(signalId);
	}
}
