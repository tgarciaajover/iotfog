package com.advicetec.configuration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SignalContainer extends Container
{

	static String sqlSelect = "SELECT id, descr, create_date, type_id, unit_id FROM setup_signal";

	public SignalContainer(String server, String user, String password) 
	{	
		super(server, user, password);	
	}
	
	public void loadContainer()
	{
		super.connect();
		super.configuationObjects.clear();

		try 
		{
			ResultSet rs = super.pst.executeQuery(sqlSelect);
			while (rs.next())
			{
				Integer id     			= rs.getInt("id");
		        String descr   			= rs.getString("descr");
		        Integer signalTypeId    = rs.getInt("type_id");
		        Integer unitId   		= rs.getInt("unit_id");
		        Timestamp timestamp = rs.getTimestamp("create_date");
		        		        
		        SignalType signalType = (SignalType) this.getReferencedObject("SignalType", signalTypeId);
		        SignalUnit signalUnit = (SignalUnit) this.getReferencedObject("Unit", signalTypeId);
		        
		        Signal object = new Signal(id);
		        object.setDescr(descr);
		        object.setType(signalType);
		        object.setUnit(signalUnit);
		        object.setCreate_date(timestamp.toLocalDateTime());
		        
		        super.configuationObjects.put(id, object);
		      
			}
			
			rs.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		super.disconnect();
	}
	
	
}
