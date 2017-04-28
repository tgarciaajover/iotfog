package com.advicetec.core;

import com.advicetec.MessageProcessor.MessageManager;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.monitorAdapter.AdapterManager;
import com.advicetec.persistence.StatusStore;
import com.advicetec.utils.MqttSubscriber;
import com.google.inject.spi.Message;
import com.advicetec.iot.rest.IotRestServer;

public class IotInit extends Configurable 
{
	
	private static IotInit instance = null;
	
	private ConfigurationManager configManager;
	private EventManager eventManager;
	private MessageManager messageManager;
	private AdapterManager adapterManager;
	private MeasuredEntityManager entityManager;
	private Thread managerThread; 
	
	private IotInit()  {
		
		super("IotInit");
		
		configManager = ConfigurationManager.getInstance();
		eventManager = EventManager.getInstance();
		messageManager = MessageManager.getInstance();
		adapterManager = AdapterManager.getInstance();
		entityManager = MeasuredEntityManager.getInstance();
		
		try {
			
			// Init the configuration  
			configManager.loadConfiguration();
			
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static IotInit getInstance(){
		if (instance == null){
			instance = new IotInit();
		}
		return instance;
	}

	public static void main(String[] args) {
		
		// caches initiation
		//		StatusStore.getInstance();
		
		IotInit iotInit = IotInit.getInstance();
		iotInit.adapterManager.run();
		System.out.println("after running adapter Manager");
		iotInit.messageManager.run();
		System.out.println("after running message Manager");
		iotInit.eventManager.run();
		System.out.println("after running event Manager");

		// Init the Rest server 
		String rest_port = iotInit.properties.getProperty("rest_port");
		try {
			IotRestServer.runServer(Integer.valueOf(rest_port));
			
			MqttSubscriber mqttSubscriber = new MqttSubscriber();
			mqttSubscriber.run();
			
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	public void getMeasuredEntityStatus(String entityID){
		MeasuredEntityFacade facade = entityManager.getFacadeOfEntityById(entityID);
		facade.getStatus();
	}
}
