package com.advicetec.configuration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.net.InetAddresses;

public class DisplayDeviceContainer extends Container 
{

	static Logger logger = LogManager.getLogger(DisplayDeviceContainer.class.getName());
	
	static String sqlSelect1 = "SELECT id, descr, ip_address, port, display_id, reference_cd FROM setup_displaydevice";
	

	public DisplayDeviceContainer(String driver, String server, String user, String password) 
	{	
		super(driver, server, user, password);	
	}
	
	public void loadContainer() throws SQLException
	{
		
		Integer id     			= 0;
	    String descr   			= null;
		Integer displayId       = 0;
	    String ipAddress		= null;
	    Integer port 			= 0;
	    String referenceCd      = null;		

		try 
		{
			super.connect();
			super.configuationObjects.clear();
						
			ResultSet rs1 = super.pst.executeQuery(sqlSelect1);
			while (rs1.next())
			{
				id     			= rs1.getInt("id");
		        descr   		= rs1.getString("descr");
				displayId       = rs1.getInt("display_id");
		        ipAddress		= rs1.getString("ip_address");
		        port 			= rs1.getInt("port");
		        referenceCd     = rs1.getString("reference_cd");

		        DisplayType displayType = (DisplayType) this.getReferencedObject("DisplayType", displayId);
		        
		        DisplayDevice object = new DisplayDevice(id);
		        object.setDescr(descr);
		        object.setDisplayType(displayType);
				object.setIpAddress(InetAddress.getByName(ipAddress));
		        object.setPort(port);
		        object.setReferenceCd(referenceCd);
		        
		        super.configuationObjects.put(id, object);
		      
			}

			rs1.close();

			super.disconnect();

		} catch (UnknownHostException e) {
			String error = "Invalid Ip address:" + ipAddress + " - Error" + e.getMessage(); 
			logger.error(error);
			e.printStackTrace();
			throw new SQLException(error);

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

	public synchronized void deleteDisplayDevice(int uniqueID)
	{
		super.configuationObjects.remove(uniqueID);
	}
	
	public synchronized void fromJSON(String json){
		
		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string and pretty print
		DisplayType displayTypeTemp;
		try {
		
			displayTypeTemp = mapper.readValue(json, DisplayType.class);
			
			super.configuationObjects.put(displayTypeTemp.getId(), displayTypeTemp);
		
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
