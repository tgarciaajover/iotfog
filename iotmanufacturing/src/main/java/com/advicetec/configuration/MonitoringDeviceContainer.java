package com.advicetec.configuration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class MonitoringDeviceContainer extends Container
{

	static Logger logger = LogManager.getLogger(MonitoringDeviceContainer.class.getName());
	
	static String sqlSelect1 = "SELECT id, device_type_id, descr, ip_address, mac_address, serial, create_date FROM setup_monitoringdevice";
	static String sqlSelect2 = "SELECT id, transformation_text, device_id, signal_type_id, port_label, refresh_time_ms,  measured_entity_id FROM setup_inputoutputport";

	private Map<String, Integer> indexByMac;
	private Map<String, Integer> indexByIpAddress;
	private Map<String, Integer> indexBySerial;

	
	public MonitoringDeviceContainer(String driver, String server, String user, String password) 
	{	
		super(driver, server, user, password);
		indexByMac = new HashMap<String, Integer>();
		indexByIpAddress = new HashMap<String, Integer>();
		indexBySerial = new HashMap<String, Integer>();
	}
	
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

	public synchronized void deleteMonitoringDevice(int uniqueID)
	{
		super.configuationObjects.remove(uniqueID);
	}
	
	
	public synchronized MonitoringDevice getByMacAddress(String macAddress)
	{
		Integer id = this.indexByMac.get(macAddress);
		if (id != null)
			return (MonitoringDevice) super.getObject(id);
		else
			return null;
		
	}
	
	public synchronized MonitoringDevice getByIpAddress(String ipAddress)
	{
		logger.debug("serach by ipaddress:" + ipAddress);
		Integer id = this.indexByIpAddress.get(ipAddress);
		if (id != null)
			return (MonitoringDevice) super.getObject(id);
		else
			return null;
		
	}

	public synchronized MonitoringDevice getBySerial(String serial)
	{
		logger.debug("serach by serial:" + serial);
		Integer id = this.indexBySerial.get(serial);
		if (id != null)
			return (MonitoringDevice) super.getObject(id);
		else
			return null;
		
	}

	public synchronized void fromJSON(String json){
		
		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string and pretty print
		MonitoringDevice mDeviceTemp;
		try {
		
			mDeviceTemp = mapper.readValue(json, MonitoringDevice.class);
			
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
			
			super.configuationObjects.put(mDeviceTemp.getId(), mDeviceTemp);
			
			logger.info("The monitoring device with Id:" + mDeviceTemp.getId() + " was inserted");
					
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
	
	}
	
}
