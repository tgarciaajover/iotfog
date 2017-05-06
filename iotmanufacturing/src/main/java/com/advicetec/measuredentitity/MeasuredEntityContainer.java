package com.advicetec.measuredentitity;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.advicetec.configuration.Container;
import com.advicetec.configuration.DeviceType;
import com.advicetec.configuration.IOSignalDeviceType;
import com.advicetec.configuration.Signal;
import com.advicetec.configuration.SignalUnitContainer;

public class MeasuredEntityContainer extends Container {

	static Logger logger = LogManager.getLogger(MeasuredEntityContainer.class.getName());
	
	static String sqlSelect1 = "SELECT id, code, descr, create_date, type FROM setup_measuredentity";
	static String sqlSelect2 = "SELECT id, name, descr, behavior_text, create_date, last_updttm, measure_entity_id FROM setup_measuredentitybehavior WHERE measure_entity_id = ";
	
	
	public MeasuredEntityContainer(String driver, String server, String user, String password) {
		super(driver, server, user, password);
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
		        String code   			= rs1.getString("code");
				String descr   			= rs1.getString("descr");
		        Timestamp timestamp 	= rs1.getTimestamp("create_date");
				String entityCategory   = rs1.getString("type");
		        		        		        
				MeasuredEntity measuredEntity = null;
				
				System.out.println("Entity Category:" + entityCategory);
				switch (entityCategory)
		        {
		           case "M":
		        	   measuredEntity = new Machine(id);
		        	   break;
		           case "P":
		        	   measuredEntity = new Plant(id);
		        	   break;
		           default:
		               System.out.println("Error entity category is not identified");
		        }
				
		        measuredEntity.setDescr(descr);
		        measuredEntity.setCode(code);
		        measuredEntity.setCreateDate(timestamp.toLocalDateTime());
		        
		        
		        super.configuationObjects.put(id, measuredEntity);
		      
			}
			
			rs1.close();

			// loop through the measured entities and load their behaviors
			
			for( Integer id : this.configuationObjects.keySet()){
				MeasuredEntity measuredEntity = (MeasuredEntity) this.configuationObjects.get(id);
				loadBehaviors(measuredEntity);
			}
			
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
	
	public void loadBehaviors(MeasuredEntity entity)
	{
		try 
		{
			String sqlSelect = sqlSelect2 + String.valueOf(entity.getId());  
			ResultSet rs2 = super.pst.executeQuery(sqlSelect);
			
			while (rs2.next()) 
			{
		        Integer id   			= rs2.getInt("id");  
		        String name            = rs2.getString("name");
		        String descr            = rs2.getString("descr");
		        String behaviorText    = rs2.getString("behavior_text");
		        
		        entity.putBehavior(id, name, descr, behaviorText);
			}
			rs2.close();

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}
	
	public MeasuredEntity fromJSON(String json) {

		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string and pretty print
		MeasuredEntity measuredEntity;
		try {
		
			measuredEntity = mapper.readValue(json, MeasuredEntity.class);

			MeasuredEntity oldMeasuredEntity = (MeasuredEntity) this.configuationObjects.get(measuredEntity.getId());
			
		    if (oldMeasuredEntity == null){
		    	this.configuationObjects.put(measuredEntity.getId(), measuredEntity);
		    	return measuredEntity;
		    	
		    } else { 
		    	//Update a measured Entity
		    	oldMeasuredEntity.updateEntityConfiguration(measuredEntity);
		    	return oldMeasuredEntity;
		    }
		
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
		
		return null;
	}	
	
		
}
