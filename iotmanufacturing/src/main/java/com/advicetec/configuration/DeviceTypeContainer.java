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

/**
 * Container for the Device Type data.
 * @author Andres Marentes
 *
 */
public class DeviceTypeContainer extends Container 
{

	static Logger logger = LogManager.getLogger(DeviceTypeContainer.class.getName());
	
	/**
	 * SQL statement used to select all device types from the database 
	 */
	static String sqlSelect1 = "SELECT id, descr, create_date FROM setup_devicetype";
	
	/**
	 * SQL statement used to select of input/output signals associated to device types in the database.
	 */
	static String sqlSelect2 = "SELECT id, i_o, device_id, signal_id  FROM setup_iosignalsdevicetype";
	

	/**
	 * Constructor for the container 
	 * 
	 * @param driver 	: driver string used to connect to the database.
	 * @param server	: Ip address of the database server 
	 * @param user		: database user
	 * @param password	: password of the user's database.
	 */
	public DeviceTypeContainer(String driver, String server, String user, String password) 
	{	
		super(driver, server, user, password);	
	}
	
	/**
	 * Load the information of device types from the database.
	 * 
	 * @throws SQLException it is trigered if some error occurs during the reading of device types.
	 */
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
		        String descr   			= rs1.getString("descr");
		        Timestamp timestamp 	= rs1.getTimestamp("create_date");
		        		        		        
		        DeviceType object = new DeviceType(id);
		        object.setDescr(descr);
		        object.setCreate_date(timestamp.toLocalDateTime());
		        
		        super.configuationObjects.put(id, object);
		      
			}
			rs1.close();

			ResultSet rs2 = super.pst.executeQuery(sqlSelect2);
			while (rs2.next())
			{
				Integer id     			= rs2.getInt("id"); 
		        String io   			= rs2.getString("i_o");
				Integer deviceId 		= rs2.getInt("device_id");
				Integer signalId		= rs2.getInt("signal_id");
		        		        		        
		        IOSignalDeviceType object = new IOSignalDeviceType(id);
		        DeviceType device = (DeviceType) this.getObject(deviceId);
		        Signal signal = (Signal) this.getReferencedObject("Signal", signalId);
		        
		        object.setI_O(io);
		        object.setSignal(signal);
		        device.putIOSignal(object);
		        		      
			}

			rs2.close();

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

	/**
	 * Removes a device type from the container.
	 * 
	 * @param uniqueID   identifier of the device type to delete from the container.
	 */
	public synchronized void deleteDeviceType(int uniqueID)
	{
		super.configuationObjects.remove(uniqueID);
	}
	
	/**
	 * Loads a device type from json 
	 * 
	 * @param json json object representing a device type.
	 */
	public synchronized void fromJSON(String json){
		
		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string and pretty print
		DeviceType deviceTypeTemp;
		try {
		
			deviceTypeTemp = mapper.readValue(json, DeviceType.class);
			SignalContainer signalContainer = (SignalContainer) this.getReferenceContainer("Signal");
			
			for (int i=0; i < deviceTypeTemp.signals.size(); i++){
				IOSignalDeviceType ioDeviceType = deviceTypeTemp.signals.get(i);
				Signal signal = (Signal) signalContainer.getObject(ioDeviceType.getSignal().getId());
				
				if (signal == null){
					signalContainer.fromJSON(ioDeviceType.getSignal().toJson());
				} else { 
					ioDeviceType.setSignal(signal);
				}
			}
			
			super.configuationObjects.put(deviceTypeTemp.getId(), deviceTypeTemp);
		
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
