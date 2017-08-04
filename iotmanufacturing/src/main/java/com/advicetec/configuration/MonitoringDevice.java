package com.advicetec.configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;

public class MonitoringDevice extends ConfigurationObject
{

	static Logger logger = LogManager.getLogger(MonitoringDevice.class.getName());
	
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

	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
    private LocalDateTime create_date;
	
	
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

	public LocalDateTime getCreate_date() {
		return create_date;
	}
	
	public void setCreate_date(LocalDateTime create_date) {
		this.create_date = create_date;
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
			if (this.inputOutputPorts.get(i).getId().equals(id)){
				return this.inputOutputPorts.get(i);
			}
		}
		
		return null;
		
	}
	
	@JsonIgnore
	public InputOutputPort getInputOutputPort(String portLabel){
		Integer id = this.portsByLabel.get(portLabel);
		return  getInputOutputPort(id);
	}
	
	@JsonIgnore
	public String getTranformation(String portLabel){
		Integer id = this.portsByLabel.get(portLabel);
		logger.debug("Port requested:" + portLabel + "Port id:" + id);
		if (getInputOutputPort(id) == null){
			logger.debug("Error Label Port: " + id + " not found");
			return null;
		} else {
			return  getInputOutputPort(id).getTransformationText();
		}
		
	}
		
	public void putInputOutputPort(InputOutputPort iop){
		this.inputOutputPorts.add(iop);
		this.portsByLabel.put(iop.getPortLabel(), iop.getId());
	}
	
	@JsonIgnore
	public String getClassName(String portLabel){
		Integer id = this.portsByLabel.get(portLabel);

		if (getInputOutputPort(id) == null){
			logger.debug("Error Label Port: " + id + " not found");
			return null;
		} else {
			return getInputOutputPort(id).getSignalType().getType().getClassName();
		}
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
