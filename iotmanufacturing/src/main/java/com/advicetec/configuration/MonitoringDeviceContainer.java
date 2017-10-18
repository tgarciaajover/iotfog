package com.advicetec.configuration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.advicetec.eventprocessor.ModBusTcpEvent;
import com.advicetec.measuredentitity.MeasuredEntity;

public class MonitoringDeviceContainer extends Container
{

	static Logger logger = LogManager.getLogger(MonitoringDeviceContainer.class.getName());
	
	/**
	 * SQL statement to select configured data of monitoring devices. 
	 */
	static String sqlSelect1 = "SELECT id, device_type_id, descr, ip_address, mac_address, serial, create_date FROM setup_monitoringdevice";
	
	/**
	 * SQL statement to select configured data of input output port related to monitoring devices. 
	 */
	static String sqlSelect2 = "SELECT id, transformation_text, device_id, signal_type_id, port_label, refresh_time_ms,  measured_entity_id FROM setup_inputoutputport";

	/**
	 * SQL statement to select modbus ports that must create new events. 
	 */
	static String sqlSelect3 = "SELECT d.ip_address, c.measured_entity_id, c.port_label, c.refresh_time_ms from setup_signal a, setup_signaltype b, setup_inputoutputport c, setup_monitoringdevice d where b.protocol = 'M' and a.type_id = b.id and c.signal_type_id = a.id and d.id = c.device_id and d.id = ";
	
	/**
	 * Maps to make faster lookups by macaddresses. Given the mac address, it returns the identifier of the measuring device configured with that address.
	 */
	private Map<String, Integer> indexByMac;
	
	/**
	 * Maps to make faster lookups by Ip address. Given the Ip address, it returns the identifier of the measuring device configured with that address.
	 */
	private Map<String, Integer> indexByIpAddress;
	
	/**
	 * Maps to make faster lookups by serial. Given the serial, it returns the identifier of the measuring device configured with that serial.
	 */
	private Map<String, Integer> indexBySerial;

	
	/**
	 * Constructor for the class, it takes as parameters data required to connect to the database.
	 * 
	 * @param driver		: driver string used to connect to the database.
	 * @param server		: Ip address of the database server
	 * @param user			: database user
	 * @param password		: password of the user's database.
	 */
	public MonitoringDeviceContainer(String driver, String server, String user, String password) 
	{	
		super(driver, server, user, password);
		indexByMac = new HashMap<String, Integer>();
		indexByIpAddress = new HashMap<String, Integer>();
		indexBySerial = new HashMap<String, Integer>();
	}
	
	/**
	 * Loads all measuring devices registered in the database into the container.
	 * 
	 * @throws SQLException
	 */
	public void loadContainer() throws SQLException
	{

		try 
		{
			super.connect();
			super.configuationObjects.clear();

			ResultSet rs1 = super.pst.executeQuery(sqlSelect1);
			while (rs1.next())
			{
				Integer id     			= rs1.getInt("id");
		        String descr   			= rs1.getString("descr");
		        Integer deviceTypeId    = rs1.getInt("device_type_id");
		        String ipAddress   		= rs1.getString("ip_address");
		        String macAddress 		= rs1.getString("mac_address");
		        String serial	 		= rs1.getString("serial");
		        Timestamp timestamp 	= rs1.getTimestamp("create_date");
		        		        
		        DeviceType deviceType = (DeviceType) this.getReferencedObject("DeviceType", deviceTypeId);
		        
		        MonitoringDevice object = new MonitoringDevice(id);
		        object.setDescr(descr);
		        object.setIp_address(ipAddress);
		        object.setMac_addres(macAddress);
		        object.setSerial(serial);
		        object.setType(deviceType);
		        object.setCreate_date(timestamp.toLocalDateTime());
		        
		        super.configuationObjects.put(id, object);
		        
		        if (macAddress != null){
			        if (!macAddress.isEmpty())
			        	indexByMac.put(macAddress,id);
		        }
		        
		        if (ipAddress != null){
			        if (!ipAddress.isEmpty())
			        	indexByIpAddress.put(ipAddress, id);
		        }
		        
		        if (serial != null){
			        if (!serial.isEmpty())
			        	indexBySerial.put(serial,id);
		        }
			}
			
			rs1.close();
			
			ResultSet rs2 = super.pst.executeQuery(sqlSelect2);
			while (rs2.next())
			{		     

				Integer id     			= rs2.getInt("id"); 
		        String transformation	= rs2.getString("transformation_text");
		        Integer deviceId    	= rs2.getInt("device_id");
		        Integer signalTypeId	= rs2.getInt("signal_type_id");
		        String portLabel		= rs2.getString("port_label");
		        Integer refreshTimeMs   = rs2.getInt("refresh_time_ms");
		        Integer measuredEntityId = rs2.getInt("measured_entity_id");
		        		        
		        MonitoringDevice device= (MonitoringDevice) this.getObject(deviceId);
		        Signal signal = (Signal)  this.getReferencedObject("Signal", signalTypeId);
		        
		        InputOutputPort port = new InputOutputPort(id);
		        port.setSignalType(signal);
		        port.setTransformationText(transformation);
		        port.setPortLabel(portLabel);
		        port.setMeasuringEntity(measuredEntityId);
		        port.setRefreshTimeMs(refreshTimeMs);
		        
		        device.putInputOutputPort(port);
			}
			
			rs2.close();
			
			super.disconnect();
			
		} catch (ClassNotFoundException e){
        	String error = "Could not find the driver class - Error" + e.getMessage(); 
        	logger.error(error);
        	e.printStackTrace();
        	throw new SQLException(error);
        } catch (SQLException e) {
        	String error = "Container:" + this.getClass().getName() +  "Error connecting to the database - error:" + e.getMessage();
        	logger.error(error);
        	e.printStackTrace();        	
        	throw new SQLException(error);
        }
		
	}
	
	/**
	 * Inserts the monitoring device given as parameter in the container.
	 * 
	 * If the monitoring device already exists, then replace it.
	 * 
	 * @param mDevice  Monitoring device to add.
	 */
	public synchronized void addMonitoringDevice(MonitoringDevice mDevice) {
		
		// Adds the monitoring device monitoring device container
		super.configuationObjects.put(mDevice.getId(), mDevice);

        // Update indexes created to references the monitoring device
		if (mDevice.getMac_addres() != null){
	        if (!mDevice.getMac_addres().isEmpty())
	        	indexByMac.put(mDevice.getMac_addres(), mDevice.getId());
        }
        
        if (mDevice.getIp_address() != null){
	        if (!mDevice.getIp_address().isEmpty())
	        	indexByIpAddress.put(mDevice.getIp_address(), mDevice.getId());
        }
        
        if (mDevice.getSerial() != null){
	        if (!mDevice.getSerial().isEmpty())
	        	indexBySerial.put(mDevice.getSerial(),mDevice.getId());
        }

	}

	/**
	 * Delete a monitoring device from the container
	 * 
	 * @param uniqueID  monitoring device identifier to remove.
	 */
	public synchronized void deleteMonitoringDevice(int uniqueID)
	{
		super.configuationObjects.remove(uniqueID);
	}
	
	
	/**
	 * Obtains a measuring device configured with the parameter mac address.  
	 * 
	 * @param macAddress  mac address to find.
	 * 
	 * @return measuring device object. If the macaddress is inexistent, the null is returned.
	 */
	public synchronized MonitoringDevice getByMacAddress(String macAddress)
	{
		Integer id = this.indexByMac.get(macAddress);
		if (id != null)
			return (MonitoringDevice) super.getObject(id);
		else
			return null;
		
	}
	
	/**
	 * Obtains a measuring device configured with the parameter IP address.  
	 * 
	 * @param ipAddress  IP address to find.
	 * 
	 * @return measuring device object. If the ipaddress is inexistent, the null is returned.
	 */
	public synchronized MonitoringDevice getByIpAddress(String ipAddress)
	{
		logger.debug("serach by ipaddress:" + ipAddress);
		Integer id = this.indexByIpAddress.get(ipAddress);
		if (id != null)
			return (MonitoringDevice) super.getObject(id);
		else
			return null;
		
	}

	/**
	 * Obtains a measuring device configured with the serial parameter.  
	 * 
	 * @param serial  serial to find.
	 * 
	 * @return measuring device object. If the serial is inexistent, the null is returned.
	 */
	public synchronized MonitoringDevice getBySerial(String serial)
	{
		logger.debug("search by serial:" + serial);
		Integer id = this.indexBySerial.get(serial);
		if (id != null)
			return (MonitoringDevice) super.getObject(id);
		else
			return null;
		
	}

	/**
	 * Builds a Measuring device from a json representation
	 * 
	 * @param json  json object representing the measuring device. 
	 * 
	 */
	public synchronized MonitoringDevice fromJSON(String json){
		
		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string and pretty print
		MonitoringDevice mDeviceTemp = null;
		
		try {
		
			mDeviceTemp = mapper.readValue(json, MonitoringDevice.class);
			
			logger.debug("Json object read:" + mDeviceTemp.toJson());
			
			logger.debug("Monitoring device id given:" + mDeviceTemp.getId()  );
			logger.debug("Monitoring address given:" + mDeviceTemp.getIp_address()  );
			
			DeviceTypeContainer deviceTypeContainer = (DeviceTypeContainer) this.getReferenceContainer("DeviceType");			
			DeviceType deviceTypeTmp = (DeviceType) deviceTypeContainer.getObject(mDeviceTemp.getType().getId());
			
			if (deviceTypeTmp != null) {
				mDeviceTemp.setType(deviceTypeTmp);
			} else {
				deviceTypeContainer.fromJSON(mDeviceTemp.getType().toJson());
			}
			
			SignalContainer signalContainer = (SignalContainer) this.getReferenceContainer("Signal");
			for (int i=0; i < mDeviceTemp.inputOutputPorts.size(); i++){
				InputOutputPort inputOutputPort = mDeviceTemp.inputOutputPorts.get(i);
				Signal signal = (Signal) signalContainer.getObject(inputOutputPort.getSignalType().getId());
				
				if (signal == null){
					signalContainer.fromJSON(inputOutputPort.getSignalType().toJson());
				} else { 
					inputOutputPort.setSignalType(signal);
				}
			}

			mDeviceTemp.updateIndexes();

		} catch (JsonParseException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return mDeviceTemp;
	}
	
	public List<ModBusTcpEvent> getModbusEvents(MonitoringDevice monitoring) throws SQLException {
		
		List<ModBusTcpEvent> events = new ArrayList<ModBusTcpEvent>();

		try 
		{
			super.connect();
			logger.info("in getModbusEvents by monitoring device - id:" + monitoring.getId() );
			
			String sqlSelect = sqlSelect3 + String.valueOf(monitoring.getId());  
			ResultSet rs3 = super.pst.executeQuery(sqlSelect);

			while (rs3.next()) 
			{
				String ipaddress        	= rs3.getString("ip_address");
				Integer measured_entity_id  = rs3.getInt("measured_entity_id");  
				String portLabel        	= rs3.getString("port_label");
				Integer refreshTimeMs       = rs3.getInt("refresh_time_ms");
				
				logger.info("new modbus event found");
				
				if (refreshTimeMs > 0){
					ModBusTcpEvent modBusEvent = ModBusTcpEvent.createModbusEvent(ipaddress, measured_entity_id, portLabel, refreshTimeMs);
					if (modBusEvent != null)
						events.add(modBusEvent);
				} else {
					logger.error("Refresh time is zero for Port label:" + portLabel + " which is invalid");
				}
			}
			
			rs3.close();

			super.disconnect();

		} catch (ClassNotFoundException e){
			String error = "Could not find the driver class - Error" + e.getMessage(); 
			logger.error(error);
			e.printStackTrace();
			throw new SQLException(error);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return events;
	}

	public Map<Integer, List<InputOutputPort>> getInputOutputPortReferingMeasuredEntity(Integer measuredEntity) {
				
		Map<Integer, List<InputOutputPort>> ports = new HashMap<Integer,List<InputOutputPort>>();

		Set<Integer> monitoringKeys = this.configuationObjects.keySet();

		for (Integer key : monitoringKeys) {
			MonitoringDevice mDevice = (MonitoringDevice) this.configuationObjects.get(key);
			ports.put(key, mDevice.getInputOutputPortReferingMeasuredEntity(measuredEntity));
		}
		
		return ports;
	}
	
}
