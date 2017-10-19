package com.advicetec.core;


import java.sql.SQLException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.advicetec.MessageProcessor.MessageManager;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.monitorAdapter.AdapterManager;
import com.advicetec.iot.rest.IotRestServer;

/**
 * Main class for the FOG.
 * 
 * This class creates instances for all system's singletons and puts to execute all managers.
 * 
 * @author Andres Marentes
 *
 */
public class IotInit extends Configurable 
{
	
	static Logger logger = LogManager.getLogger(IotInit.class.getName());
	
	/**
	 * Class instance. 
	 */
	private static IotInit instance = null;
	
	/**
	 * Reference to configuration manager instance
	 */
	private ConfigurationManager configManager;
	
	/**
	 * Reference to event manager instance
	 */
	private EventManager eventManager;
	
	/**
	 * Reference to message manager instance
	 */
	private MessageManager messageManager;
	
	/**
	 * Reference to adapter manager instance
	 */
	private AdapterManager adapterManager;
	
	/**
	 * Reference to entity manager instance
	 */
	private MeasuredEntityManager entityManager;
		
	/**
	 * Constructor for the class, it performs the following steps:
	 * 
	 * 		1. Loads the properties file IotInit.properties
	 * 		2. Load the configurable objects from the database.
	 * 		3. puts to execute adapter, message and event processors.
	 * 		
	 * @throws SQLException
	 */
	private IotInit() throws SQLException  {
		
		super("IotInit");
		
		// Property configurator
		
		logger.info("Start IOT engine");
		
		configManager = ConfigurationManager.getInstance();
		
		logger.info("Configuration Manager loaded");
		
		eventManager = EventManager.getInstance();
		
		logger.info("Event Manager loaded");
		
		messageManager = MessageManager.getInstance();
		
		logger.info("Message Manager loaded");
		
		adapterManager = AdapterManager.getInstance();
		
		logger.info("Adapter Manager loaded");
		
		entityManager = MeasuredEntityManager.getInstance();
		
		logger.info("Entity Manager loaded");

		// Init the configuration  
		configManager.loadConfiguration();
		
		logger.info("IOT engine Loaded");
		
	}
	
	/**
	 * Gets the instance of the IoT class, where we put to execute all sub-components. 
	 * 
	 * @return instance of the class.
	 * 
	 * @throws SQLException
	 */
	public static IotInit getInstance() throws SQLException{
		if (instance == null){
			instance = new IotInit();
		}
		return instance;
	}

	/**
	 * 
	 * Main for the FOG, it creates the iot instance and creates a REST server to listen for new request.  
	 *  
	 * @param args  we don't require any parameter.
	 */
	public static void main(String[] args) {
				
		try {

			IotInit iotInit = IotInit.getInstance();
			iotInit.adapterManager.run();
			logger.info("after running adapter Manager");
			iotInit.messageManager.run();
			logger.info("after running message Manager");
			iotInit.eventManager.run();
			logger.info("after running event Manager");

			// Init the Rest server 
			String rest_port = iotInit.properties.getProperty("rest_port");
			
			IotRestServer.runServer(Integer.valueOf(rest_port));
						
			
		} catch (NumberFormatException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} 
	}

}
