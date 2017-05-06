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

public class ReasonCodeContainer extends Container 
{

	static Logger logger = LogManager.getLogger(ReasonCodeContainer.class.getName());
	
	static String sqlSelect = "SELECT id, descr, classification, down, create_date, group_cd FROM setup_idlereason";

	public ReasonCodeContainer(String driver, String server, String user, String password) 
	{	
		super(driver, server, user, password);	
	}
	
	public void loadContainer() throws SQLException
	{

		try 
		{
			super.connect();
			super.configuationObjects.clear();

			ResultSet rs = super.pst.executeQuery(sqlSelect);
			while (rs.next())
			{
				Integer id     			= rs.getInt("id");
		        String descr   			= rs.getString ("descr");
		        String classification   = rs.getString ("classification");
		        String down			    = rs.getString ("down");
		        Timestamp timestamp     = rs.getTimestamp("create_date");
		        String group_cd			= rs.getString ("group_cd");
		        
		        ReasonCode reasonCode = new ReasonCode(id, descr);
		        reasonCode.setClassification(classification);
		        reasonCode.setGroup(group_cd);
		        
		        if (down == "Y"){
		        	reasonCode.setIdleDown(true);
		        } else {
		        	reasonCode.setIdleDown(false);
		        }
		        reasonCode.setCreateDate(timestamp.toLocalDateTime());
		        
		        super.configuationObjects.put(id, reasonCode);
		      
			}
			
			rs.close();

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

	public void deleteReasonCode(int uniqueID)
	{
		super.configuationObjects.remove(uniqueID);
	}
	
	public void fromJSON(String json){
		
		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string and pretty print
		ReasonCode reasonCode;
		try {
		
			reasonCode = mapper.readValue(json, ReasonCode.class);
			super.configuationObjects.put(reasonCode.getId(), reasonCode);
		
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
