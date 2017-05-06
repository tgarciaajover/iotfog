package com.advicetec.configuration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class SignalContainer extends Container
{

	static Logger logger = LogManager.getLogger(SignalContainer.class.getName());
	
	static String sqlSelect = "SELECT id, descr, create_date, type_id, unit_id FROM setup_signal";

	public SignalContainer(String driver, String server, String user, String password) 
	{	
		super(driver, server, user, password);	
	}
	
	public void loadContainer() throws SQLException
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
			logger.error(e.getMessage());
			e.printStackTrace();
			throw e;
		}
		
		super.disconnect();
	}

	public void deleteSignal(int uniqueID)
	{
		super.configuationObjects.remove(uniqueID);
	}

	public void fromJSON(String json){
		
		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string and pretty print
		Signal signalTemp;
		try {
		
			signalTemp = mapper.readValue(json, Signal.class);
			
			SignalTypeContainer signalTypeContainer = (SignalTypeContainer) this.getReferenceContainer("SignalType");
						
			ConfigurationObject signalType = signalTypeContainer.getObject(signalTemp.getType().getId());
	        
	        if (signalType != null){
	        	// Overwrites the signal type with the one in the container
	        	signalTemp.setType((SignalType) signalType);
	        } else {
	        	// Creates the signal type in the container
	        	signalTypeContainer.fromJSON(signalTemp.getType().toJson());
	        }

			SignalUnitContainer signalUnitContainer = (SignalUnitContainer) this.getReferenceContainer("Unit");
			ConfigurationObject signalUnit = signalUnitContainer.getObject(signalTemp.getUnit().getId());
			
	        // Overwrites the signal unit.
	        if (signalUnit != null){
	        	signalTemp.setUnit((SignalUnit) signalUnit);
	        } else {
	        	signalUnitContainer.fromJSON(signalTemp.getUnit().toJson()); 
	        }
	        
			super.configuationObjects.put(signalTemp.getId(), signalTemp);
		
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
