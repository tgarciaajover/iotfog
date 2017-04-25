package com.advicetec.configuration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

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
	
	public void fromJSON(String json){
		
		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string and pretty print
		Signal signalTemp;
		try {
		
			signalTemp = mapper.readValue(json, Signal.class);
			
			Integer signalTypeId = signalTemp.getType().getId();
			Integer unitId = signalTemp.getUnit().getId();
			
			ConfigurationObject signalType = this.getReferencedObject("SignalType", signalTypeId);
			ConfigurationObject signalUnit = (SignalUnit) this.getReferencedObject("Unit", unitId);
	        
	        if (signalType != null){
	        	// Overwrites the signal type with the one in the container
	        	signalTemp.setType((SignalType) signalType);
	        } else {
	        	// Creates the signal type in the container
	        	this.addReferencedObject("SignalType", signalTemp.getType());
	        }

	        // Overwrites the signal unit.
	        if (signalUnit != null){
	        	signalTemp.setUnit((SignalUnit) signalUnit);
	        } else {
	        	this.addReferencedObject("Unit", signalTemp.getUnit());
	        }
	        
			super.configuationObjects.put(signalTemp.getId(), signalTemp);
		
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
