package com.advicetec.configuration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Container class for Display Devices.
 * 
 * @author andres
 *
 */
public class DisplayDeviceContainer extends Container 
{

	static Logger logger = LogManager.getLogger(DisplayDeviceContainer.class.getName());
	
	/**
	 * SQL Statement for reading the configuration data of display devices.
	 */
	static String sqlSelect1 = "SELECT id, descr, ip_address, port, display_id, reference_cd FROM setup_displaydevice";
	
	/**
	 * Constructor for the class, it takes as parameters data required to connect to the database.
	 * @param driver	: driver string used to connect to the database.
	 * @param server	: Ip address of the database server
	 * @param user		: database user
	 * @param password	: password of the user's database.
	 */
	public DisplayDeviceContainer(String driver, String server, String user, String password) 
	{	
		super(driver, server, user, password);	
	}
	
	/**
	 * Loads all display devices registered in the database into the container.
	 * @throws SQLException This exception is triggered whenever some problem occur during connection establishment.
	 */
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
		        
		        if (displayType == null)
		        	logger.error("Display type not found" + displayId);
		        
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

	/**
	 * Delete a display device from the container
	 * @param uniqueID  Identifier of the display device to remove.
	 */
	public synchronized void deleteDisplayDevice(int uniqueID)
	{
		super.configuationObjects.remove(uniqueID);
	}
	
	/**
	 * Gets a display device from its alternative identifier
	 * 
	 * @param referenceCd  alternative identifier
	 * @return  Display device object with the alternative identifier.
	 */
	public synchronized DisplayDevice getDisplayDevice(String referenceCd){
		
		for (int displayDeviceId : this.configuationObjects.keySet()){
			String gerReference = ((DisplayDevice) this.configuationObjects.get(displayDeviceId)).getReferenceCd();
			if (gerReference.compareTo(referenceCd) == 0) {
				return (DisplayDevice) this.configuationObjects.get(displayDeviceId);
			}
		}
		
		return null;
	}
	
	/**
	 * Builds a display device from json object representation. Once it creates the new instance, that instance is inserted in the container
	 * 
	 * @param json  json representation.
	 */
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
