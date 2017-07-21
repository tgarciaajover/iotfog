package com.advicetec.measuredentitity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.Configurable;
/**
 * Manages the table that stores OEE aggregation.
 * @author iot
 *
 */
public class OEEAggregationManager  extends Configurable {

	static Logger logger = LogManager.getLogger(OEEAggregationManager.class.getName());
	
	private static OEEAggregationManager instance = null;
	private OEEAggregationContainer oeeAggregations;
	
	public OEEAggregationManager() {
		super("OEEAggregation");
		String driver = properties.getProperty("driver");
		String server = properties.getProperty("server");
		String user = properties.getProperty("user");
		String password = properties.getProperty("password");

		oeeAggregations = new OEEAggregationContainer(driver, server, user, password);
	}

	
	public static OEEAggregationManager getInstance()
	{
		if(instance == null){
			instance = new OEEAggregationManager();
		}
		return instance;
	}	
	
	public OEEAggregationContainer getOeeAggregationContainer()
	{
		return this.oeeAggregations;
	}
	
	
}
