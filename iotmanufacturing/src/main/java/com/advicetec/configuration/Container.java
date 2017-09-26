package com.advicetec.configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * This class is the parent for all containers classes supporting the interaction with the database.
 * @author Andres Marentes
 *
 */
public abstract class Container 
{
	
	static Logger logger = LogManager.getLogger(Container.class.getName());
	
	// Database connection parameters
	/**
	 * String used to connect to the database.
	 */
	private String driver;
	
	/**
	 * IP Address database server 
	 */
	private String server;
	
	/**
	 * User of the database, it has privileges to all configuration object tables.
	 */
	private String user;
	
	/**
	 * Password of the database user. 
	 */
	private String password;
		
	/**
	 * Connection object.
	 */
	private Connection conn; 
    
	/**
	 * Prepare statement object.
	 */
	protected Statement pst;
	
    /**
     * Map with all the configured objects retrieved from the database, This maps is in the form key of the  configurable object, configurable object.
     */
    protected Map <Integer,ConfigurationObject> configuationObjects;
    
    /**
     * This map is used to emulate the referential integrity of the database. As configurable objects refer to other configurable objects, then we create a reference
     * in this map to those references. 
     */
    private Map<String, Container> references;
    
	/**
	 * Constructor for the class.
	 * 
	 * @param driverStr 	driver string used to connect to the database.
	 * @param server		Ip address of the database server 
	 * @param user			database user
	 * @param password		password of the user's database.
	 */
	public Container(String driverStr,  String server, String user, String password) {
		super();
		this.driver = driverStr;
		this.server = server;
		this.user = user;
		this.password = password;
		
		logger.debug("driver:" + driver + " server:" + server + " user:" + user + " password:" + password);
		
		this.conn = null; 
	    this.pst= null;

	    configuationObjects = new HashMap<Integer,ConfigurationObject>();
	    references = new HashMap<String,Container>();
	}

	/**
	 * Add a new reference to a configuration object container.
	 * @param field			Field that maintains the reference
	 * @param container		Container being referenced.
	 */
	protected synchronized void addReference(String field, Container container)
	{
		references.put(field, container);
	}

	/**
	 * Get the configuration object which is referenced in the field given by field and id given in parameter id.
	 * 
	 * @param field field defining the foreigh key 
	 * @param id  identifier of the object in teh foreigh table.
	 * 
	 * @return Configurable object referenced.
	 */
	protected synchronized ConfigurationObject getReferencedObject(String field, Integer id)
	{				
		return references.get(field).getObject(id);
	}
	
	/**
	 * Adds a configuration object being referenced. 
	 * 
	 * @param field		field defining the foreigh key 
	 * @param object	Configuration object being referenced.
	 */
	protected synchronized void addReferencedObject(String field, ConfigurationObject object)
	{
		(references.get(field)).configuationObjects.put(object.getId(), object);
	}
	
	/**
	 * Performs the action of connecting to the database with a non prepared statement.
	 * 
	 * @throws SQLException  			it is triggered if some problem occurs during connection establishment.
	 * @throws ClassNotFoundException   it is triggered if the driver given does not correspond to a valid connection class.
	 */
	protected void connect() throws SQLException, ClassNotFoundException
	{

		if (this.driver == null){
			String error = "No driver was specified - Error";
			logger.error(error);
			throw new SQLException(error);
		}
		Class.forName(this.driver);
		conn = DriverManager.getConnection(this.server, this.user, this.password);

		conn.setAutoCommit(false);

		pst = conn.createStatement();
						

	}
	
	/**
	 * Performs the action of connecting to the database with a prepared statement.
	 * 
	 * @param sqlText  					prepare statement to used in the connection.
	 * @throws SQLException				it is triggered if some problem occurs during connection establishment.
	 * @throws ClassNotFoundException 	it is triggered if the driver given does not correspond to a valid connection class.
	 */
	protected void connect_prepared(String sqlText) throws SQLException, ClassNotFoundException
	{
		if (this.driver == null){
			String error = "No driver was specified - Error";
			logger.error(error);
			throw new SQLException(error);
		}
		
		Class.forName(this.driver);
		
		conn = DriverManager.getConnection(this.server, this.user, this.password);

		conn.setAutoCommit(false);

		pst = conn.prepareStatement(sqlText);
		
	}
	
	/**
	 * Creates a new prepared statement once it has been connected
	 * 
	 * @param sqlText			prepare statement to used in the connection.
	 * @throws SQLException		it is triggered if some problem occurs with the prepared statement.
	 */
	protected void prepare_statement(String sqlText) throws SQLException{
		
		if ((conn != null) && (!conn.isClosed()))
			pst = conn.prepareStatement(sqlText);
	}
	
	
	/**
	 * Performs a commit in the database.
	 * 
	 * @throws SQLException		it is triggered if some problem occurs during commit.
	 */
	protected void commit() throws SQLException{
		conn.commit();
	}

	/**
	 * Performs the database disconnection
	 */
	protected void disconnect()
	{
		if(pst!=null)
        {
            try
            {
                pst.close();
            } catch (SQLException e) {
            	logger.error("Prepare statement is already close - error:" + e.getMessage());
                e.printStackTrace();
            }
        }
         
        if(conn!=null) 
        {
            try
            {
                conn.close();
            } catch (SQLException e) {
            	logger.error("Database Connection is already close - error:" + e.getMessage());
                e.printStackTrace();
            }
        }	
	}
	
	/**
	 * Gets the database driver
	 * 
	 * @return database driver
	 */
	public synchronized String getDbDriver() {
		return this.driver;
	}
	
	/**
	 * Gets the url used to connect to the database 
	 * 
	 * @return database url.
	 */
	public synchronized String getServer() {
		return this.server;
	}
	
	/**
	 * Gets the database user 
	 * 
	 * @return databasr user
	 */
	public synchronized String getUser() {
		return this.user;
	}
	
	/**
	 * Gets the database password
	 * 
	 * @return database password
	 */
	public synchronized String getPassword() {
		return this.password;
	}
	
	/**
	 * Get the keys from the configurable objects registered in the container
	 * 
	 * @return list of configurable keys registered.
	 */
	public synchronized Set<Integer> getKeys(){
		return configuationObjects.keySet(); 
	}
	
	/**
	 * Get the configurable object with identifier id.
	 * 
	 * @param id  	identifier of the configurable object.
	 * @return  	Configurable object or null if it does not exist.
	 */
	public synchronized ConfigurationObject getObject(Integer id)
	{
		return configuationObjects.get(id); 
	}
	
	/**
	 * Get the container being referenced with the field given as parameter
	 * 
	 * @param field  field being used to reference the container.
	 * @return  Container being referenced.
	 */
	public synchronized Container getReferenceContainer(String field)
	{
		return references.get(field); 
	}

	/**
	 * Remove the configurable object from the container.
	 * 
	 * @param id  identifier of the configurable object to remove.
	 * @return true if the object was removed, false otherwise.
	 */
	public synchronized boolean removeObject(Integer id)
	{
		if (this.configuationObjects.remove(id) == null)
			return false;
		else 
			return true;
	}
	
	/**
	 * Return the number of object registered in the container
	 * 
	 * @return number of object registered.
	 */
	public synchronized int size()
	{
		return configuationObjects.size();
	}
	
	/**
	 * @return Returns  the string driver used.
	 */
	public synchronized String getDriver()
	{
		return this.driver;
	}
}
