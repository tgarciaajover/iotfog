package com.advicetec.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class MonitoringDevice extends ConfigurationObject
{

	@JsonProperty("device_type") 
	private DeviceType type;
	
	@JsonProperty("descr") 
	private String descr;
	
	@JsonProperty("serial") 
	private String serial;
	
	@JsonProperty("mac_address") 
	private String mac_addres;
	
	@JsonProperty("ip_address") 
	private String ip_address;
	
	@JsonProperty("io_ports")
	protected List<InputOutputPort> inputOutputPorts;
	
	private Map<String, Integer> portsByLabel; 
	
	@JsonCreator
	public MonitoringDevice(@JsonProperty("id") Integer id) {
		super(id);
		inputOutputPorts = new ArrayList<InputOutputPort>();
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
	
	@JsonIgnore
	public InputOutputPort getInputOutputPort(Integer id){

		for (int i = 0; i < this.inputOutputPorts.size(); i++){
			if (this.inputOutputPorts.get(i).getId() == id){
				return this.inputOutputPorts.get(i);
			}
		}
		
		return null;
		
	}
	
	@JsonIgnore
	public InputOutputPort getInputOutputPort(String portLabel){
		Integer id = this.portsByLabel.get(portsByLabel);
		return  getInputOutputPort(id);
	}
	
	@JsonIgnore
	public String getTranformation(String portLabel){
		Integer id = this.portsByLabel.get(portsByLabel);
		return  getInputOutputPort(id).getTransformationText();
	}
	
	@JsonIgnore
	public String getBehavior(String portLabel){
		Integer id = this.portsByLabel.get(portsByLabel);
		return  getInputOutputPort(id).getBehaviorText();		
	}
	
	public void putInputOutputPort(InputOutputPort iop){
		this.inputOutputPorts.add(iop);
		this.portsByLabel.put(iop.getPortLabel(), iop.getId());
	}
	
	@JsonIgnore
	public String getClassName(String portLabel){
		Integer id = this.portsByLabel.get(portsByLabel);
		return  getInputOutputPort(id).getSignalType().getType().getClassName();
	}

	public String toJson()
	{
		ObjectMapper mapper = new ObjectMapper();
			
		String jsonInString=null;
		try {
			
			jsonInString = mapper.writeValueAsString(this);
			
			
		} catch (JsonGenerationException e) {
			// TODO: log the error
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return jsonInString;
	}
	
}
