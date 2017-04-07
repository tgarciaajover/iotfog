package com.advicetec.MessageProcessor;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.core.Manager;
import com.advicetec.eventprocessor.EventManager;

public class MessageManager extends Manager 
{

	private static MessageManager instance=null;
	private ConfigurationManager confManager = null;
	private EventManager evntManager = null;
	
	public static MessageManager getInstance()
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
			Thread t = new Thread(new MessageHandler(instance.getQueue(), evntManager.getQueue()));
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
