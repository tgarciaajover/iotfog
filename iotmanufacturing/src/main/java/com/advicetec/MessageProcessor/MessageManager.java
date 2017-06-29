package com.advicetec.MessageProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.core.Manager;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.iot.rest.MonitoringDeviceResource;

public class MessageManager extends Manager 
{

	static Logger logger = LogManager.getLogger(MessageManager.class.getName());
	
	private static MessageManager instance=null;
	private ConfigurationManager confManager = null;
	private BlockingQueue delayedQueue = null;
	
	public static MessageManager getInstance()
	{
		if (instance==null)
			instance = new MessageManager(); 
		return instance;
	}
	
	private MessageManager() 
	{
		super("MessageManager");	
		this.confManager = ConfigurationManager.getInstance();
		this.delayedQueue = new DelayQueue();
	}	
	
	public void run() 
	{
		logger.debug("Start Message Manager run");
		int number = Integer.valueOf(getProperty("NumProcessHandlers"));
		List<Thread> listThread =  new ArrayList<Thread>();
		
		for (int i = 0; i < number; i++) 
		{
			Thread t = new Thread( new MessageHandler(instance.getQueue(), this.delayedQueue) );
			t.start();
			listThread.add(t);
		}
		
		Thread delayConsumer = new Thread(new DelayQueueConsumer("ProcessConsumer", this.delayedQueue));
		delayConsumer.start();
		/*
		try {
			delayConsumer.join();
			for (Thread t : listThread){
				t.join();
			}	
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		logger.debug("Ending Message Manager run ");
	}	

}
