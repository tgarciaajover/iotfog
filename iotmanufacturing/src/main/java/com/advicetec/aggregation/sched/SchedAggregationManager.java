package com.advicetec.aggregation.sched;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.Configurable;

/**
 * Manager for the SchedAggregation object.
 * 
 * The manager object is represented as singleton, so the rest of the SW can deal with these kind of objects.
 * 
 * @author Jose Pulgarin 
 *
 */
public class SchedAggregationManager  extends Configurable {

	static Logger logger = LogManager.getLogger(SchedAggregationManager.class.getName());
	
	/**
	 * Singleton Instance 
	 */
	private static SchedAggregationManager instance = null;

	private static int MIN_DB_THREAD_POOL = 5;
	
	private static int MAX_DB_THREAD_POOL = 30; 
	
	/**
	 * Reference to the SchedAggregation container. Through this object we can insert, update, and delete SchedAggregations.
	 */
	private SchedAggregationContainer schedAggregations;
	
	/**
	 *  This class is configurable through a properties file called OEEAggregation.properties
	 *  The required properties are those for connecting to the database: 
	 *  	driver : sql string used to connect the database
	 *  	server: Ip address or database server's name 
	 *  	user: user in the database
	 *  	password: password for the user in the DB. 
	 */
	public SchedAggregationManager() {
		super("SchedAggregation");
		String driver = properties.getProperty("driver");
		String server = properties.getProperty("server");
		String user = properties.getProperty("user");
		String password = properties.getProperty("password");

		if (properties.getProperty("min_db_thread_pool") != null)
			MIN_DB_THREAD_POOL = Integer.parseInt(properties.getProperty("min_db_thread_pool"));

		if (properties.getProperty("max_db_thread_pool") != null)
			MAX_DB_THREAD_POOL = Integer.parseInt(properties.getProperty("max_db_thread_pool"));
		
		schedAggregations = new SchedAggregationContainer(driver, server, user, password, MIN_DB_THREAD_POOL, MAX_DB_THREAD_POOL);
	}
	
	/**
	 * @return OEEAggregationManager instance
	 */
	public static SchedAggregationManager getInstance()
	{
		if(instance == null){
			instance = new SchedAggregationManager();
		}
		return instance;
	}	
	
	/**
	 * @return Returns the reference to the OEEAggregationContainer.
	 */
	public SchedAggregationContainer getSchedAggregationContainer()
	{
		return this.schedAggregations;
	}
	
	
}
