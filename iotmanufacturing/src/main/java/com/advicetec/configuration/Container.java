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

import com.advicetec.core.Configurable;
import com.advicetec.language.behavior.BehaviorDefPhase;


/**
 * This class stores all configuration objects into a database.
 * @author user
 *
 */
public abstract class Container 
{
	
	static Logger logger = LogManager.getLogger(Container.class.getName());
	
	// Database connection parameters
	private String driver;
	private String server;
	private String user;
	private String password;
		
	private Connection conn; 
    protected Statement pst;
	
    protected Map <Integer,ConfigurationObject> configuationObjects;
    private Map<String, Container> references;
    
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

	protected synchronized void addReference(String field, Container container)
	{
		references.put(field, container);
	}

	protected synchronized ConfigurationObject getReferencedObject(String field, Integer id)
	{				
		return references.get(field).getObject(id);
	}
	
	protected synchronized void addReferencedObject(String field, ConfigurationObject object)
	{
		(references.get(field)).configuationObjects.put(object.getId(), object);
	}
	
	protected void connect() throws SQLException
	{

        try
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
						
        } catch(ClassNotFoundException e){
        	String error = "Could not find the driver class - Error" + e.getMessage(); 
        	logger.error(error);
        	e.printStackTrace();
        	throw new SQLException(error);
        } catch(SQLException e){
        	String error = "Error connecting to the database - error:" + e.getMessage();
        	logger.error(error);
        	e.printStackTrace();        	
        	throw new SQLException(error);
        }

	}

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
	
	public synchronized Set<Integer> getKeys(){
		return configuationObjects.keySet(); 
	}
	
	public synchronized ConfigurationObject getObject(Integer id)
	{
		return configuationObjects.get(id); 
	}
	
	public synchronized Container getReferenceContainer(String field)
	{
		return references.get(field); 
	}
	
	public synchronized int size()
	{
		return configuationObjects.size();
	}
}
