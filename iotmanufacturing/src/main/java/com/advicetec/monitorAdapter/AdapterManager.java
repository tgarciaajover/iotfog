package com.advicetec.monitorAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.MessageManager;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.core.Manager;
import com.advicetec.monitorAdapter.protocolconverter.MqttDigital;

public class AdapterManager extends Manager implements Runnable
{

	static Logger logger = LogManager.getLogger(AdapterManager.class.getName());
	
	private static AdapterManager instance=null;
	private static ConfigurationManager confManager = null; 
	private static MessageManager messManager = null;
	
	public static AdapterManager getInstance()
	{
		if (instance==null)
			instance = new AdapterManager();
		
		return instance;
	}
	
	private AdapterManager() 
	{
		super("AdapterManager");	
		confManager = ConfigurationManager.getInstance();
		messManager = MessageManager.getInstance();
	}

	public void run() 
	{
		logger.debug("Starting Adapter Manager run");
		int number = Integer.valueOf(getProperty("NumAdapterHandlers")); 
		for (int i = 0; i < number; i++) 
		{
			Thread t = new Thread(new AdapterHandler(instance.getQueue(),messManager.getQueue()));
			t.start();
			/*
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		}
		
		logger.debug("Ending Adapter Manager run");
	}
	
}
