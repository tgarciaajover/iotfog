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
	private Map<String, Integer> portsByLabel; 
	
	public MonitoringDevice(Integer id) {
		super(id);
		inputOutputPorts = new HashMap<Integer, InputOutputPort>();
		portsByLabel = new HashMap<String, Integer>();
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
	
	public InputOutputPort getInputOutputPort(String portLabel){
		Integer id = this.portsByLabel.get(portsByLabel);
		return  getInputOutputPort(id);
	}
	
	public String getTranformation(String portLabel){
		Integer id = this.portsByLabel.get(portsByLabel);
		return  getInputOutputPort(id).getTransformationText();
	}
	
	public String getBehavior(String portLabel){
		Integer id = this.portsByLabel.get(portsByLabel);
		return  getInputOutputPort(id).getBehaviorText();		
	}
	
	public void putInputOutputPort(InputOutputPort iop){
		this.inputOutputPorts.put(iop.getId(), iop);
		this.portsByLabel.put(iop.getPortLabel(), iop.getId());
	}
	
	public String getClassName(String portLabel){
		Integer id = this.portsByLabel.get(portsByLabel);
		return  getInputOutputPort(id).getSignalType().getType().getClassName();
	}
	
}
