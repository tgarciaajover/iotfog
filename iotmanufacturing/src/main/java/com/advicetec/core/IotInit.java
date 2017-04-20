package com.advicetec.core;

import com.advicetec.MessageProcessor.MessageManager;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.monitorAdapter.AdapterManager;
import com.advicetec.persistence.StatusStore;
import com.google.inject.spi.Message;

public class IotInit {
	
	private static IotInit instance = null;
	
	private ConfigurationManager configManager;
	private EventManager eventManager;
	private MessageManager messageManager;
	private AdapterManager adapterManager;
	private MeasuredEntityManager entityManager;
	private Thread managerThread; 
	
	private IotInit() {
		configManager = ConfigurationManager.getInstance();
		eventManager = EventManager.getInstance();
		messageManager = MessageManager.getInstance();
		adapterManager = AdapterManager.getInstance();
		entityManager = MeasuredEntityManager.getInstance();
		managerThread = new Thread(); 	
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
		
		AdapterManager adapterManager = AdapterManager.getInstance();
		Thread managerThread = new Thread(); 
	}

	public void getMeasuredEntityStatus(String entityID){
		MeasuredEntityFacade facade = entityManager.getFacadeOfEntityById(entityID);
		facade.getStatus();
	}
}
