package com.advicetec.MessageProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.core.Manager;

/**
 * This class implements a singleton instance to manage a pool of Message 
 * Handlers.
 * It sets the configuration from the <code>.properties</code> file, and 
 * initializes all Message Handlers.
 * <p>
 * Some special events are reprogrammed for being executed at some point 
 * future. They are called <i>delayed</i> events. 
 * Those events are scheduled to specific time/hour, or to some time from now  
 * as timer events.
 * This class maintains an inventory of delayed events that has a key with the
 * type of event, and a mapped boolean value if it exists in the inventory.
 * <p> 
 * The <code>run</code> method creates the pool of threads assigned to 
 * <code>MessageHandler</code>, and instantiates a thread for managing delayed 
 * events.
 * 
 * Later the <code>AdapterHandler</code> uses this object's queue as origin 
 * queue and <code>MessageManager</code> as destination queue.
 * 
 * @see AdapterManager
 * @see MessageHandler
 */
public class MessageManager extends Manager 
{

	static Logger logger = LogManager.getLogger(MessageManager.class.getName());
	
	private static MessageManager instance=null;
	private ConfigurationManager confManager = null;
	/**
	 * Queue for delayed events.
	 */
	private BlockingQueue<DelayEvent> delayedQueue = null;
	/**
	 * Inventory of delayed events.
	 */
	private ConcurrentHashMap<String, Boolean> delayedTypeEvents = null;
	/**
	 * Returns the singleton instance of this manager.
	 * @return the singleton instance of this manager.
	 */
	public static MessageManager getInstance()
	{
		if (instance==null)
			instance = new MessageManager(); 
		return instance;
	}
	/**
	 * Sets the configuration from the .properties file.
	 * Creates a singleton instance of AdapterManager and also sets the 
	 * instance of MessageManager.
	 */
	private MessageManager() 
	{
		super("MessageManager");	
		this.confManager = ConfigurationManager.getInstance();
		this.delayedQueue = new DelayQueue<DelayEvent>();
		this.delayedTypeEvents = new ConcurrentHashMap<String, Boolean>();
	}	
	
	/**
	 * 
	 */
	public void run() 
	{
		logger.debug("Start Message Manager run");
		int number = Integer.valueOf(getProperty("NumProcessHandlers"));
		List<Thread> listThread =  new ArrayList<Thread>();
		// creates threads for each message handler
		for (int i = 0; i < number; i++) 
		{
			Thread t = new Thread( new MessageHandler(instance.getQueue(), this.delayedQueue) );
			t.start();
			listThread.add(t);
		}
		// Thread to manage delayed events
		Thread delayConsumer = new Thread(new DelayQueueConsumer("ProcessConsumer", this.delayedQueue));
		delayConsumer.start();
		
		logger.debug("Ending Message Manager run ");
	}
	
	/**
	 * Returns <code>TRUE</code> if there is an event with the type specified 
	 * as parameter, <code>FALSE</code> otherwise.
	 * @param type key for delayEvent map.
	 * @return <code>TRUE</code> if there is an event with the type specified 
	 * as parameter, <code>FALSE</code> otherwise.
	 */
	public boolean existDelayEventType(String type){
		if (this.delayedTypeEvents.get(type) == null)
			return false;
		else 
			return true;
	}
	
	/**
	 * Removes a delayed event from the inventory.
	 * @param type key for delayed event.
	 */
	public void removeDelayEventType(String type){
		this.delayedTypeEvents.remove(type);
	}
	
	/**
	 * Add a key (type) for delayed events to the inventory.
	 * @param type key for a delayed event.
	 */
	public void addDelayEventType(String type){
		this.delayedTypeEvents.put(type, new Boolean(true));
	}

}
