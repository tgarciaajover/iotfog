package com.advicetec.MessageProcessor;

import com.advicetec.FogClasses.Manager;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.monitorAdapter.AdapterHandler;
import com.advicetec.monitorAdapter.AdapterManager;

public class MessageManager extends Manager 
{

	private static MessageManager instance=null;
	private static ConfigurationManager confManager = null;
	private static EventManager evntManager = null;
	
	public MessageManager getInstance()
	{
		if (instance==null)
			instance = new MessageManager(); 
		return instance;
	}
	
	private MessageManager() 
	{
		super("MessageManager");	
		confManager = ConfigurationManager.getInstance();
		evntManager = EventManager.getInstance();
	}	
	
	public void run() {
		
		int number = Integer.valueOf(getProperty("NumProcessHandlers")); 
		for (int i = 0; i < number; i++) 
		{
			Thread t = new Thread(new MessageHandler());
			
		}
	}	
}
