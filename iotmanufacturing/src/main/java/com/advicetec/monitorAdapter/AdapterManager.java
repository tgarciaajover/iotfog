package com.advicetec.monitorAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.MessageManager;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.core.Manager;

/**
 * This class implements a singleton instance to manage a pool of Adapter 
 * Handlers.
 * It sets the configuration from the <code>.properties</code> file, and 
 * initializes the Adapter Handlers.
 * <p>
 * The <code>run</code> method creates the pool of threads assigned to 
 * AdapterHandlers.
 * Later the <code>AdapterHandler</code> uses this object's queue as origin 
 * queue and <code>MessageManager</code> as destination queue.
 * 
 * @see MessageManager
 * @see AdapterHandler
 */
public class AdapterManager extends Manager implements Runnable
{

	static Logger logger = LogManager.getLogger(AdapterManager.class.getName());
	
	private static AdapterManager instance=null;
	private static ConfigurationManager confManager = null; 
	private static MessageManager messManager = null;

	/**
	 * Returns the singleton instance of this manager.
	 * @return the singleton instance of this manager.
	 */
	public synchronized static AdapterManager getInstance()
	{
		if (instance==null)
			instance = new AdapterManager();
		
		return instance;
	}
	
	/**
	 * Sets the configuration from the .properties file.
	 * Creates a singleton instance of AdapterManager and also sets the 
	 * instance of MessageManager.
	 */
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
		}
		
		logger.debug("Ending Adapter Manager run");
	}
	
}
