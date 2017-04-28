package com.advicetec.measuredentitity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.advicetec.configuration.Container;
import com.advicetec.configuration.DeviceType;
import com.advicetec.configuration.IOSignalDeviceType;
import com.advicetec.configuration.Signal;

public class MeasuredEntityContainer extends Container {

	static String sqlSelect1 = "SELECT id, code, descr, create_date, type FROM setup_measuredentity";
	static String sqlSelect2 = "SELECT id, name, descr, behavior_text, create_date, last_updttm, measure_entity_id FROM setup_measuredentitybehavior";
	
	
	public MeasuredEntityContainer(String server, String user, String password) {
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
				String id     			= String.valueOf(rs1.getInt("id"));  
		        String code   			= rs1.getString("code");
				String descr   			= rs1.getString("descr");
		        Timestamp timestamp 	= rs1.getTimestamp("create_date");
				String entityCategory   = rs1.getString("type");
		        		        		        
				MeasuredEntity measuredEntity =null;
				switch (entityCategory)
		        {
		           case "M":
		        	   measuredEntity = new Machine(id);
		        	   break;
		           case "P":
		        	   //TODO: crear la planta.
		        	   break;
		           default:
		               System.out.println("Error entity category is not identified");
		        }
				
		        measuredEntity.setDescr(descr);
		        measuredEntity.setCode(code);
		        measuredEntity.setCreateDate(timestamp.toLocalDateTime());
		        
		        super.configuationObjects.put(id, object);
		      
			}
			rs1.close();

			ResultSet rs2 = super.pst.executeQuery(sqlSelect2);
			while (rs2.next())
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
	
	
}
