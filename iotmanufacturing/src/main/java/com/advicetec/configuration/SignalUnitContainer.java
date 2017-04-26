package com.advicetec.configuration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;


public class SignalUnitContainer extends Container 
{

	static String sqlSelect = "SELECT id, descr, create_date FROM setup_signalunit";

	public SignalUnitContainer(String server, String user, String password) 
	{	
		super(server, user, password);	
	}
	
	public void loadContainer()
	{
		super.connect();
		super.configuationObjects.clear();

		try {
			ResultSet rs = super.pst.executeQuery(sqlSelect);
			while (rs.next())
			{
				Integer id     = rs.getInt    ("id");
		        String descr   = rs.getString ("descr");
		        Timestamp timestamp = rs.getTimestamp("create_date");
		        
		        SignalUnit object = new SignalUnit(id);
		        object.setDescr(descr);
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
		SignalUnit unitTemp;
		try {
		
			unitTemp = mapper.readValue(json, SignalUnit.class);
			super.configuationObjects.put(unitTemp.getId(), unitTemp);
		
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
