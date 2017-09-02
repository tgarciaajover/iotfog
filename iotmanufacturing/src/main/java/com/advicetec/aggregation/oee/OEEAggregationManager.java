package com.advicetec.aggregation.oee;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.Configurable;

/**
 * Manager for the OEE aggregation object.
 * 
 * The manager object is represented as singleton, so the rest of the SW can deal with these kind of objects.
 * 
 * @author Andres Marentes
 *
 */
public class OEEAggregationManager  extends Configurable {

	static Logger logger = LogManager.getLogger(OEEAggregationManager.class.getName());
	
	/**
	 * Singleton Instance 
	 */
	private static OEEAggregationManager instance = null;
	
	/**
	 * Reference to the OEE aggregation container. Through this object we can insert, update, and delete OEE aggregations.
	 */
	private OEEAggregationContainer oeeAggregations;
	
	/**
	 *  This class is configurable through a properties file called OEEAggregation.properties
	 *  The required properties are those for connecting to the database: 
	 *  	driver : sql string used to connect the database
	 *  	server: Ip address or database server's name 
	 *  	user: user in the database
	 *  	password: password for the user in the DB. 
	 */
	public OEEAggregationManager() {
		super("OEEAggregation");
		String driver = properties.getProperty("driver");
		String server = properties.getProperty("server");
		String user = properties.getProperty("user");
		String password = properties.getProperty("password");

		oeeAggregations = new OEEAggregationContainer(driver, server, user, password);
	}

	/**
	 * @return OEEAggregationManager instance
	 */
	public static OEEAggregationManager getInstance()
	{
		if(instance == null){
			instance = new OEEAggregationManager();
		}
		return instance;
	}	
	
	/**
	 * @return Returns the reference to the OEEAggregationContainer.
	 */
	public OEEAggregationContainer getOeeAggregationContainer()
	{
		return this.oeeAggregations;
	}
	
	
}
