package com.advicetec.eventprocessor;

import com.advicetec.MessageProcessor.MessageHandler;
import com.advicetec.MessageProcessor.MessageManager;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.core.Manager;
import com.advicetec.monitorAdapter.AdapterManager;

public class EventManager extends Manager
{

	private static EventManager instance=null;
	private static ConfigurationManager confManager = null;
	
	public static EventManager getInstance()
	{
		if (instance==null)
			instance = new EventManager(); 
		return instance;
	}

	private EventManager() 
	{
		super("EventManager");	
		confManager = ConfigurationManager.getInstance();
	}	

	public void run() 
	{
		
		int number = Integer.valueOf(getProperty("NumProcessHandlers")); 
		for (int i = 0; i < number; i++) 
		{
			Thread t = new Thread(new EventHandler(instance.getQueue()));
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}	

}
