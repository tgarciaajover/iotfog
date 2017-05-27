package com.advicetec.eventprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

import com.advicetec.MessageProcessor.DelayQueueConsumer;
import com.advicetec.MessageProcessor.MessageHandler;
import com.advicetec.MessageProcessor.MessageManager;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.core.Manager;
import com.advicetec.monitorAdapter.AdapterManager;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;


public class EventManager extends Manager
{

	private static EventManager instance=null;
	private static ConfigurationManager confManager = null;
	private BlockingQueue delayedQueue = null;
	private int maxModbusConnections = 0;
	private int numActiveConnections = 0;
	private List<TCPMasterConnection> availableConnections = null;
	private List<TCPMasterConnection> usedConnections = null;
		
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
		this.delayedQueue = new DelayQueue();
		
		// This list contains the connections available to be used by any handler.
		this.availableConnections = new ArrayList<TCPMasterConnection>();
		
		// This list contains the connections being used by some handler
		this.usedConnections = new ArrayList<TCPMasterConnection>();
		
		// Maximum number of modbus connections. 
		int maxModbusConnections = Integer.valueOf(getProperty("MaxModbusConnections")); 
	}	

	public void run() 
	{
		System.out.println("Starting Event Manager run");
		
		int number = Integer.valueOf(getProperty("NumProcessHandlers")); 
		List<Thread> listThread =  new ArrayList<Thread>();
		
		for (int i = 0; i < number; i++) 
		{
			Thread t = new Thread(new EventHandler(instance.getQueue(), this.delayedQueue));
			t.start();
			listThread.add(t);
		}

		Thread delayConsumer = new Thread(new DelayQueueConsumer("EventConsumer", this.delayedQueue));
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
		
		System.out.println("Ending Event Manager run");
	}	

	private synchronized TCPMasterConnection getModbusConnection()
	{
		if (numActiveConnections <= maxModbusConnections)
		
		else{
			return null;
		}
	}
}
