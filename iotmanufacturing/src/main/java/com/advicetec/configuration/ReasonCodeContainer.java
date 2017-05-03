package com.advicetec.configuration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class ReasonCodeContainer extends Container 
{

	static String sqlSelect = "SELECT id, descr, classification, down, create_date, group_cd FROM setup_idlereason";

	public ReasonCodeContainer(String server, String user, String password) 
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
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		super.disconnect();
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
