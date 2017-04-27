package com.advicetec.configuration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class DeviceTypeContainer extends Container 
{

	static String sqlSelect1 = "SELECT id, descr, create_date FROM setup_devicetype";
	static String sqlSelect2 = "SELECT id, i_o, device_id, signal_id  FROM setup_iosignalsdevicetype";

	public DeviceTypeContainer(String server, String user, String password) 
	{	
		super(server, user, password);	
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
		        Timestamp timestamp 	= rs1.getTimestamp("create_date");
		        		        		        
		        DeviceType object = new DeviceType(id);
		        object.setDescr(descr);
		        object.setCreate_date(timestamp.toLocalDateTime());
		        
		        super.configuationObjects.put(id, object);
		      
			}
			rs1.close();

			ResultSet rs2 = super.pst.executeQuery(sqlSelect2);
			while (rs1.next())
			{
				Integer id     			= rs2.getInt("id"); 
		        String io   			= rs2.getString("i_o");
				Integer deviceId 		= rs2.getInt("device_id");
				Integer signalId		= rs2.getInt("signal_id");
		        		        		        
		        IOSignalDeviceType object = new IOSignalDeviceType(id);
		        DeviceType device = (DeviceType) this.getObject(deviceId);
		        Signal signal = (Signal) this.getReferencedObject("Signal", signalId);
		        
		        object.setI_O(io);
		        object.setSignal(signal);
		        device.putIOSignal(object);
		        		      
			}
			rs2.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		super.disconnect();
	}

	public synchronized void deleteDeviceType(int uniqueID)
	{
		super.configuationObjects.remove(uniqueID);
	}
	
	public synchronized void fromJSON(String json){
		
		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string and pretty print
		DeviceType deviceTypeTemp;
		try {
		
			deviceTypeTemp = mapper.readValue(json, DeviceType.class);
			SignalContainer signalContainer = (SignalContainer) this.getReferenceContainer("Signal");
			
			for (int i=0; i < deviceTypeTemp.signals.size(); i++){
				IOSignalDeviceType ioDeviceType = deviceTypeTemp.signals.get(i);
				Signal signal = (Signal) signalContainer.getObject(ioDeviceType.getSignal().getId());
				
				if (signal == null){
					System.out.println("estoy aqui signal null");
					signalContainer.fromJSON(ioDeviceType.getSignal().toJson());
				} else { 
					System.out.println("estoy aqui signal not null");
					ioDeviceType.setSignal(signal);
				}
			}
			
			super.configuationObjects.put(deviceTypeTemp.getId(), deviceTypeTemp);
		
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
