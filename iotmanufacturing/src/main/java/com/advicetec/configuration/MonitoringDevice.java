package com.advicetec.configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

/**
 * A monitoring device represents the entity installed for a machine that is actually receiving and sending information to sensors.
 * 
 * For the system, the important parts are: 
 * 	- how to identify this object; such as, addresses (mac or ip), serial, etc.. 
 *  - Its different ports configured.  
 * 
 * @author Andrés Marentes
 *
 */
public abstract class MonitoringDevice extends ConfigurationObject
{
 
	static Logger logger = LogManager.getLogger(MonitoringDevice.class.getName());
	
	/**
	 * Device type which conforms this measuring device. This measuring device is an instance of the device type template. 
	 */
	@JsonProperty("device_type") 
	private DeviceType type;
	
	/**
	 * Description of this measuring device.
	 */
	@JsonProperty("descr") 
	private String descr;
	
	/**
	 * Equipment's physical serial 
	 */
	@JsonProperty("serial") 
	private String serial;
	
	/**
	 * Equipment's mac address
	 */
	@JsonProperty("mac_address") 
	private String macAddress;
	
	/**
	 * Equipment's IP address (Ipv4)
	 */
	@JsonProperty("ip_address") 
	private String ipAddress;
	
	@JsonProperty("type")
	private MonitoringDeviceType monitoringDeviceType;
	
	/**
	 * Date and time when the equipment was registered in the system.
	 */
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
    private LocalDateTime createDate;
	
		
	/**
	 * Constructor for the class. It receives the identifier of the measuring entity.
	 * @param id Identifier of the measuring entity.
	 */
	@JsonCreator
	public MonitoringDevice(@JsonProperty("id") Integer id) {
		super(id);
	}
	
	/**
	 * Gets the device type 
	 * @return  device type.
	 */
	public DeviceType getType() {
		return type;
	}
	
	/**
	 * Sets the device type
	 * 
	 * @param type  device type to set.
	 */
	public void setType(DeviceType type) {
		this.type = type;
	}
	
	/**
	 * Gets the description of the measuring entity
	 * 
	 * @return measuring entity description.
	 */
	public String getDescr() {
		return descr;
	}
	
	/**
	 * Sets the description of the measuring entity
	 * 
	 * @param descr  description to be set.
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}
	
	/**
	 * Gets the equipment's serial 
	 * @return  serial
	 */
	public String getSerial() {
		return serial;
	}
	
	/**
	 * Sets the equipment's serial 
	 * @param serial
	 */
	public void setSerial(String serial) {
		this.serial = serial;
	}

	/**
	 * Gets the date and time when the equipment was registered in the system.
	 * @return data and time registration date.
	 */
	public LocalDateTime getCreate_date() {
		return createDate;
	}
	
	/**
	 * Sets the date and time when the equipment was registered in the system.
	 * 
	 * @param create_date  registration datetime.
	 */
	public void setCreate_date(LocalDateTime create_date) {
		this.createDate = create_date;
	}  
	
	/**
	 * Gets the equipment's mac address
	 * 
	 * @return mac address.
	 */
	public String getMac_addres() {
		return macAddress;
	}
	
	/**
	 * Sets the equipment's mac address.
	 * @param mac_addres 
	 */
	public void setMac_addres(String mac_addres) {
		this.macAddress = mac_addres;
	}
	
	/**
	 * Gets the equipment's Ip Address
	 * @return equipment Ip address
	 */
	public String getIp_address() {
		return ipAddress;
	}
	
	/**
	 * Sets the equipment's IP Address
	 * @param ip_address
	 */
	public void setIp_address(String ip_address) {
		this.ipAddress = ip_address;
	}
	
	/**
	 * Gets the input output port by identifier
	 * @param id identifier of the input - output port.
	 * @return input output port object or null if there is not a port with that identifier.
	 */
	@JsonIgnore
	public abstract InputOutputPort getInputOutputPort(Integer id);
				
	/**
	 * Adds a port into the list of input output ports.
	 * 
	 * @param iop Input - Output port object to add.
	 */
	public abstract void putInputOutputPort(InputOutputPort iop);
			
	public abstract List<InputOutputPort> getInputOutputPortReferingMeasuredEntity(Integer measuredEntity);
	
	/**
	 * Creates a JSON representation for the object.
	 * 
	 * @return Json representation.
	 */
	public String toJson()
	{
		ObjectMapper mapper = new ObjectMapper();
			
		String jsonInString=null;
		try {
			
			jsonInString = mapper.writeValueAsString(this);
			
			
		} catch (JsonGenerationException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return jsonInString;
	}

	public MonitoringDeviceType getMonitoringDeviceType() 
	{
		return monitoringDeviceType;
	}

	public void setMonitoringDeviceType(MonitoringDeviceType monitoringDeviceType) 
	{
		this.monitoringDeviceType = monitoringDeviceType;
	}

	
	
}
