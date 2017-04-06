package com.advicetec.configuration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SignalTypeContainer extends Container 
{

	static String sqlSelect = "SELECT id, name, class_name FROM setup_signaltype";

	public SignalTypeContainer(String server, String user, String password) 
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
				Integer id     = rs.getInt    ("id");
		        String name   = rs.getString ("name");
		        String class_name   = rs.getString ("class_name");
		        
		        SignalType object = new SignalType(id);
		        object.setName(name);
		        object.setClassName(class_name);
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
