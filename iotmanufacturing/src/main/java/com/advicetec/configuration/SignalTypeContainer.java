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

public class SignalTypeContainer extends Container 
{

	static Logger logger = LogManager.getLogger(SignalTypeContainer.class.getName());

	/**
	 *  SQL statement to select the signal type configuration data  
	 */
	static String sqlSelect = "SELECT id, name, class_name, protocol, create_date FROM setup_signaltype";

	/**
	 * Constructor for the class, it takes as parameters data required to connect to the database.
	 * 
	 * @param driver		: driver string used to connect to the database.
	 * @param server		: Ip address of the database server
	 * @param user			: database user
	 * @param password		: password of the user's database.
	 */
	public SignalTypeContainer(String driver, String server, String user, String password) 
	{	
		super(driver, server, user, password);	
	}
	
	/**
	 * Loads all signal types registered in the database into the container.
	 * 
	 * @throws SQLException
	 */
	public void loadContainer() throws SQLException
	{

		try 
		{
			super.connect();
			super.configuationObjects.clear();

			
			ResultSet rs = super.pst.executeQuery(sqlSelect);
			while (rs.next())
			{
				Integer id     = rs.getInt    ("id");
		        String name   = rs.getString ("name");
		        String class_name   = rs.getString ("class_name");
		        String protocol = rs.getString("protocol");
		        Timestamp timestamp = rs.getTimestamp("create_date");
		        
		        SignalType object = new SignalType(id);
		        object.setName(name);
		        object.setClassName(class_name);
		        object.setCreate_date(timestamp.toLocalDateTime());
		        object.setProtocol(protocol);
		        super.configuationObjects.put(id, object);
		      
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

	/**
	 * Delete a signal type from the container
	 * 
	 * @param uniqueID  Identifier of the signal to remove.
	 */
	public void deleteSignalType(int uniqueID)
	{
		super.configuationObjects.remove(uniqueID);
	}
	
	/**
	 * Builds a signal type object from Json object representation. Once it creates the new instance, it is inserted in the container
	 * 
	 * @param json  json representation.
	 */
	public void fromJSON(String json){
		
		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string and pretty print
		SignalType sTypeTemp;
		try {
		
			sTypeTemp = mapper.readValue(json, SignalType.class);
			super.configuationObjects.put(sTypeTemp.getId(), sTypeTemp);
		
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
