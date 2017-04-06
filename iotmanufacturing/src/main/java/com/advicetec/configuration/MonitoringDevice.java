package com.advicetec.configuration;

import java.util.HashMap;
import java.util.Map;

public class MonitoringDevice extends ConfigurationObject
{

	private DeviceType type;
	private String descr;
	private String serial;
	private String mac_addres;
	private String ip_address;
	private Map<Integer, InputOutputPort> inputOutputPorts;
	
	public MonitoringDevice(Integer id) {
		super(id);
		inputOutputPorts = new HashMap<Integer, InputOutputPort>();
	}
	
	public DeviceType getType() {
		return type;
	}
	public void setType(DeviceType type) {
		this.type = type;
	}
	public String getDescr() {
		return descr;
	}
	public void setDescr(String descr) {
		this.descr = descr;
	}
	public String getSerial() {
		return serial;
	}
	public void setSerial(String serial) {
		this.serial = serial;
	}
	public String getMac_addres() {
		return mac_addres;
	}
	public void setMac_addres(String mac_addres) {
		this.mac_addres = mac_addres;
	}
	public String getIp_address() {
		return ip_address;
	}
	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}
	
	public InputOutputPort getInputOutputPort(Integer id){
		return this.inputOutputPorts.get(id);
	}
	
	public void putInputOutputPort(InputOutputPort iop){
		this.inputOutputPorts.put(iop.getId(), iop);
	}
	
}
