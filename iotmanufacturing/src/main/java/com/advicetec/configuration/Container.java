package com.advicetec.configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * This class stores all configuration objects into a database.
 * @author user
 *
 */
public abstract class Container 
{
		
	// Database connection parameters
	private String server;
	private String user;
	private String password;
		
	private Connection conn; 
    protected Statement pst;
	
    protected Map <Integer,ConfigurationObject> configuationObjects;
    private Map<String, Container> references;
    
	public Container(String server, String user, String password) {
		super();
		this.server = server;
		this.user = user;
		this.password = password;
		
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
	
	protected void connect()
	{

        try
        {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(this.server, this.user, this.password);
			
			conn.setAutoCommit(false);
									
			pst = conn.createStatement();
						
        } catch(Exception e){
        	e.printStackTrace();
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
                e.printStackTrace();
            }
        }
         
        if(conn!=null) 
        {
            try
            {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }	
	}
	
	
	public synchronized ConfigurationObject getObject(Integer id)
	{
		return configuationObjects.get(id); 
	}
}
