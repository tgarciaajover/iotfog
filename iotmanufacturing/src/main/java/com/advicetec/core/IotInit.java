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
import com.advicetec.utils.MqttSubscriber;
import com.advicetec.iot.rest.IotRestServer;

public class IotInit extends Configurable 
{
	
	static Logger logger = LogManager.getLogger(IotInit.class.getName());
	
	private static IotInit instance = null;
	
	private ConfigurationManager configManager;
	private EventManager eventManager;
	private MessageManager messageManager;
	private AdapterManager adapterManager;
	private MeasuredEntityManager entityManager;
	private Thread managerThread; 
	
	private IotInit() throws SQLException  {
		
		super("IotInit");
		
		// Property configurator
		
		logger.info("Start IOT engine");
		
		configManager = ConfigurationManager.getInstance();
		eventManager = EventManager.getInstance();
		messageManager = MessageManager.getInstance();
		adapterManager = AdapterManager.getInstance();
		entityManager = MeasuredEntityManager.getInstance();

		// Init the configuration  
		configManager.loadConfiguration();
		
		logger.info("IOT engine Loaded");
		
	}
	
	public static IotInit getInstance() throws SQLException{
		if (instance == null){
			instance = new IotInit();
		}
		return instance;
	}

	public static void main(String[] args) {
		
		// caches initiation
		//		StatusStore.getInstance();
		
		try {

			IotInit iotInit = IotInit.getInstance();
			iotInit.adapterManager.run();
			logger.debug("after running adapter Manager");
			iotInit.messageManager.run();
			logger.debug("after running message Manager");
			iotInit.eventManager.run();
			logger.debug("after running event Manager");

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

	public void getMeasuredEntityStatus(Integer entityID){
		MeasuredEntityFacade facade = entityManager.getFacadeOfEntityById(entityID);
		facade.getStatus();
	}
}
