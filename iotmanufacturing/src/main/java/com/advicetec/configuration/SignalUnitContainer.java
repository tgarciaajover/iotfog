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
 * Container class for Signal Units.
 * 
 * @author Andres Marentes
 *
 */
public class SignalUnitContainer extends Container 
{

	static Logger logger = LogManager.getLogger(SignalUnitContainer.class.getName());
	
	/**
	 * SQL Statement for selecting configuration data of signals units. 
	 */
	static String sqlSelect = "SELECT id, descr, create_date FROM setup_signalunit";

	/**
	 * Constructor for the class, it takes as parameters data required to connect to the database.
	 * 
	 * @param driver	: driver string used to connect to the database.
	 * @param server	: Ip address of the database server
	 * @param user		: database user
	 * @param password	: password of the user's database.
	 */
	public SignalUnitContainer(String driver, String server, String user, String password) 
	{	
		super(driver, server, user, password);	
	}
	
	/**
	 * Loads all signal unitss registered in the database into the container.
	 * 
	 * @throws SQLException
	 */
	public void loadContainer() throws SQLException
	{

		try {

			super.connect();
			super.configuationObjects.clear();

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
	 * Deletes a signal unit from the container
	 * 
	 * @param uniqueID Identifier of the signal unit to remove.
	 */
	public void deleteSignalUnit(int uniqueID)
	{
		super.configuationObjects.remove(uniqueID);
	}
	
	/**
	 * Builds a signal unit from Json object representation. Once it creates the new instance, it is inserted in the container
	 * 
	 * @param json  json representation.
	 */
	public void fromJSON(String json){
		
		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string and pretty print
		SignalUnit unitTemp;
		try {
		
			unitTemp = mapper.readValue(json, SignalUnit.class);
			super.configuationObjects.put(unitTemp.getId(), unitTemp);
		
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
