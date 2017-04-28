package com.advicetec.configuration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class MonitoringDeviceContainer extends Container
{

	static String sqlSelect1 = "SELECT id, device_type_id, descr, ip_address, mac_address, serial, create_date FROM setup_monitoringdevice";
	static String sqlSelect2 = "SELECT id, transformation_text, device_id, signal_type_id, port_label, measured_entity_id FROM setup_inputoutputport";

	private Map<String, Integer> indexByMac;

	
	public MonitoringDeviceContainer(String server, String user, String password) 
	{	
		super(server, user, password);
		indexByMac = new HashMap<String, Integer>();
	}
	
	public void loadContainer()
	{
		super.connect();
		super.configuationObjects.clear();

		try 
		{
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
		        indexByMac.put(macAddress,id);
		      
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
		        Integer measuredEntityId = rs2.getInt("measured_entity_id");
		        
		        System.out.println("id:" + id);
		        System.out.println("portLabel:" + portLabel);
		        
		        MonitoringDevice device= (MonitoringDevice) this.getObject(deviceId);
		        Signal signal = (Signal)  this.getReferencedObject("Signal", signalTypeId);
		        
		        InputOutputPort port = new InputOutputPort(id);
		        port.setSignalType(signal);
		        port.setTransformationText(transformation);
		        port.setPortLabel(portLabel);
		        port.setMeasuringEntity(measuredEntityId);
		        
		        device.putInputOutputPort(port);
			}
			
			rs2.close();
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		super.disconnect();
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
					System.out.println("estoy aqui signal null");
					signalContainer.fromJSON(inputOutputPort.getSignalType().toJson());
				} else { 
					System.out.println("estoy aqui signal not null");
					inputOutputPort.setSignalType(signal);
				}
			}
			
			super.configuationObjects.put(mDeviceTemp.getId(), mDeviceTemp);
					
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
}
