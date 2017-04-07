package com.advicetec.configuration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class MonitoringDeviceContainer extends Container
{

	static String sqlSelect1 = "SELECT id, type_id, descr, ip_address, mac_address, serial FROM setup_monitoringdevice";
	static String sqlSelect2 = "SELECT id, transformation_text, device_id, signal_type_id, behavior_text, port_label, measured_entity_id FROM setup_inputoutputport";

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
		        Integer deviceTypeId    = rs1.getInt("type_id");
		        String ipAddress   		= rs1.getString("ip_address");
		        String macAddress 		= rs1.getString("mac_address");
		        String serial	 		= rs1.getString("serial");
		        		        
		        DeviceType deviceType = (DeviceType) this.getReferencedObject("DeviceType", deviceTypeId);
		        
		        MonitoringDevice object = new MonitoringDevice(id);
		        object.setDescr(descr);
		        object.setIp_address(ipAddress);
		        object.setMac_addres(macAddress);
		        object.setSerial(serial);
		        object.setType(deviceType);
		        
		        super.configuationObjects.put(id, object);
		        indexByMac.put(macAddress,id);
		      
			}
			
			rs1.close();
			
			ResultSet rs2 = super.pst.executeQuery(sqlSelect2);
			while (rs1.next())
			{
				Integer id     			= rs2.getInt("id"); 
		        String transformation	= rs2.getString("transformation_text");
		        Integer deviceId    	= rs2.getInt("device_id");
		        Integer signalTypeId	= rs2.getInt("signal_type_id");
		        String behaviorText		= rs2.getString("behavior_text");
		        String portLabel		= rs2.getString("port_label");
		        String measuredEntityId = rs2.getString("measured_entity_id");
		        		        
		        MonitoringDevice device= (MonitoringDevice) this.getObject(deviceId);
		        Signal signal = (Signal)  this.getReferencedObject("Signal", signalTypeId);
		        
		        InputOutputPort port = new InputOutputPort(id);
		        port.setSignalType(signal);
		        port.setTransformationText(transformation);
		        port.setBehaviorText(behaviorText);
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

	public synchronized MonitoringDevice getByMacAddress(String macAddress)
	{
		Integer id = this.indexByMac.get(macAddress);
		if (id != null)
			return (MonitoringDevice) super.getObject(id);
		else
			return null;
		
	}
}
