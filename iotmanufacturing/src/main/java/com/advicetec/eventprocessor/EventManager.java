package com.advicetec.eventprocessor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.MessageProcessor.DelayQueueConsumer;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.core.Manager;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.mpmcqueue.QueueType;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;

import java.util.Iterator;

/**
 * This class manages all event handlers. To manage includes: create the event handlers and put them to execute. 
 * 
 * @author Andres Marentes
 *
 */
/**
 * @author andres
 *
 */
public class EventManager extends Manager
{

	static Logger logger = LogManager.getLogger(EventManager.class.getName());
	
	/**
	 * Singleton Instance 
	 */
	private static EventManager instance=null;
		
	/**
	 * This is the reference to the queue of events to be processed.
	 */
	private BlockingQueue<DelayEvent> delayedQueue = null;
	
	
	/**
	 * Max number modbus connections to be able to process at the same time. This number is configured by means of the properties file 
	 */
	private int maxModbusConnections = 0;
	
	
	/**
	 * This the is the time in seconds that is used to renew modbus connections. If the connection is open for more than timeOut seconds
	 * then the connection is closed and open again.
	 */
	private int timeOut = 0; 
	
	
	/**
	 * This hashmap contains the available connections for the duple (ipadress,key) in the key of the hashmap. 
	 */
	private Map<String, Stack<Map.Entry<LocalDateTime,TCPMasterConnection>> > availableConnections = null;
	
	/**
	 * This hashmap contains the used connections for the duple (ipadress, key) in the key of the hashmap.
	 */
	private Map<String, Stack<Map.Entry<LocalDateTime,TCPMasterConnection>>> usedConnections = null;
	
	/**
	 * This map maintains the number of current handlers being executed by type of Event
	 */	
	private Map<EventType, Integer> numActiveHandlers;

	/**
	 * This map maintains the number of maximum handlers that can be executed for a type of Event
	 */
	private Map<EventType, Integer> maximimActiveHandlers;
	
	/**
	 *  Number of handlers that should be started to process events.
	 */
	private int numHandlers = 10;
	
	/**
	 * Returns the singleton instance for the class, if not created then creates the instance. 
	 * @return Event manager singleton.
	 */
	public synchronized static EventManager getInstance()
	{
		if (instance==null)
			instance = new EventManager(); 
		return instance;
	}

	/**
	 * Event manager constructor. 
	 * This class reads the properties file from a file that must be under resource folder called EventManager.properties
	 * The expected options are:
	 * 		MaxModbusConnections : Maximum number of connections that can be open at the same time. 
	 * 							   If all the modbus slaves are in a different (ip_address, port) then put the number of ports as the value.
	 * 
	 *      timeOut: timeout to refresh a modbus connection. 
	 */
	private EventManager() 
	{
		
		super("EventManager");	

		logger.info("Constructor Event Manager");

		this.delayedQueue = new DelayQueue<DelayEvent>();

		
		String numProcessHandlers = getProperty("NumProcessHandlers");
		if (numProcessHandlers != null){
			try {
				
				numHandlers = Integer.parseInt(numProcessHandlers);

			} catch (NumberFormatException e) {
				logger.error("The value given in NumProcessHandlers is not a valid number");
				System.exit(0);
			}
		}
		
		// This list contains the connections available to be used by any handler.
		this.availableConnections = new HashMap<String, Stack<Map.Entry<LocalDateTime,TCPMasterConnection>>>();
		
		// This list contains the connections being used by some handler
		this.usedConnections = new HashMap<String, Stack<Map.Entry<LocalDateTime,TCPMasterConnection>>>();
		
		// Maximum number of modbus connections.
		String strMaxModbusConnections = getProperty("MaxModbusConnections");
		if (strMaxModbusConnections != null){
			strMaxModbusConnections = strMaxModbusConnections.replaceAll("\\s","");
			maxModbusConnections = Integer.valueOf(strMaxModbusConnections); 
		} else {
			logger.error("maxModbus connection field not established in event manager config file");
			System.exit(0);
		}
		
		String strTimeOut = getProperty("TimeOut");
		if (strTimeOut != null){
			strTimeOut = strTimeOut.replaceAll("\\s","");
			timeOut = Integer.valueOf(strTimeOut);
		} else {
			logger.error("timeout field not established in event manager config file");
			System.exit(0);			
		}

		logger.info("timeout given:" + String.valueOf(timeOut));

		// Reads the maximum handlers by type of event.
		this.maximimActiveHandlers = new HashMap<EventType, Integer>();		
		for (Entry<Integer, EventType> eventType : EventType.getList()) { 
			String limitStr = getProperty(eventType.getValue().getName());
			if (limitStr != null) {
				int limit = Integer.parseInt(limitStr);
				this.maximimActiveHandlers.put(eventType.getValue(), new Integer(limit));
			}
		}
		
		// Initialize the map of current count active handlers
		numActiveHandlers = new HashMap<EventType, Integer>();
		
		
	}	

	/**
	 * This method puts to execute all event handlers as well as the delay consumer instance.
	 * Once all handlers are running, it finishes.
	 */
	public void run() 
	{
		logger.info("Starting Event Manager run");
		
		List<Thread> listThread =  new ArrayList<Thread>();
	
		for (int i = 0; i < numHandlers; i++) 
		{
			Thread t = new Thread(new EventHandler(instance.getQueue(), this.delayedQueue));
			t.start();
			listThread.add(t);
		}

		Thread delayConsumer = new Thread(new DelayQueueConsumer("EventConsumer", this.delayedQueue));
		delayConsumer.start();

		logger.info("Ending Event Manager run");
	}	

	/**
	 * This method verifies that connections are not too old, in case of timeout then it refreshes the connection. 
	 * @param con. Connection to verify 
	 * @return an updated connection. 
	 * @throws Exception This exception is triggered when the connection can not be establised.
	 */
	private LocalDateTime getActiveModbusConnection(Map.Entry<LocalDateTime,TCPMasterConnection> con) throws Exception
	{
		
		logger.debug("in getActiveModbusConnection");
		
		LocalDateTime start = con.getKey();
		if (start.plusNanos(con.getValue().getTimeout()* 1000000).isBefore(LocalDateTime.now())){
			logger.debug("Reconnecting to modbus slave");
			con.getValue().close();
			con.getValue().connect();
			return LocalDateTime.now();
		} else {
			return con.getKey();
		}
	}
	
	/**
	 * Get a connection from the pool of active modbus connections. 
	 * In case that maxmodbus connection has been reached, it returns null.  
	 * Works for ipv4
	 * 
	 * @param ipAddress Ipv4 address of the modbus slave
	 * @param port : port where the slave is listening to.
	 * @return  a modbus TCP connection 
	 * @throws UnknownHostException. The slave can not be reached through the network.
	 */
	public synchronized TCPMasterConnection getModbusConnection(String ipAddress, int port) throws UnknownHostException
	{
		logger.debug("in getModbusConnection  address: " + ipAddress + " port: " + port  );
		
		String key = ipAddress + ":" + Integer.toString(port);
		
		int usedCon = 0;
		int avilCon = 0;
		if (this.availableConnections.get(key) != null)
			avilCon = this.availableConnections.get(key).size();
			
		
		if (this.usedConnections.get(key) != null)
			usedCon = this.usedConnections.get(key).size();
		
		if (usedCon + avilCon <= maxModbusConnections){
			if (avilCon > 0)
			{
				
				logger.debug("There are avail connections - availCon: " + avilCon + " usedConnection: " + String.valueOf((usedCon)));
				
				// Remove the connection from available connections
				Map.Entry<LocalDateTime,TCPMasterConnection> ret = availableConnections.get(key).pop();
				
				try{
					// Update the connection (reconnect or maintain the connection).
					LocalDateTime start = getActiveModbusConnection(ret);
					
					logger.debug("The available connection stated at:" + start.toString() );
					
					// Create the entry.
					Map.Entry<LocalDateTime,TCPMasterConnection> newEntry = new AbstractMap.SimpleEntry<LocalDateTime,TCPMasterConnection>(start, ret.getValue()); 
					
					// Verifies that there exists a node for the ip_address, port
					if (!(this.usedConnections.containsKey(key))){
						Stack<Map.Entry<LocalDateTime,TCPMasterConnection>> list = new Stack<Map.Entry<LocalDateTime,TCPMasterConnection>>();
						this.usedConnections.put(key, list);
					}
	
					// Insert the connection into the used connections.
					this.usedConnections.get(key).push(newEntry);
										
					// return the connection.
					return newEntry.getValue();

				} catch (Exception e) {
					logger.error("could not connect with the modbus slave with ipaddress" + ipAddress + " port:" + Integer.toString(port), " Message:" + e.getMessage());
					return null;
				}
				
			} else {
				
				logger.debug("There are not avail connections - availCon: " + avilCon);
				
				// Create a new connection and return it.
	    		TCPMasterConnection con = null; //the connection
	    		
	    		// Create the address object from the string.
	    		InetAddress addr =  InetAddress.getByName(ipAddress); //the slave's address
	    		try {
	    			
	    			logger.debug("Ipaddress: " + addr.toString());
	    			
	    			// Creates the connection to modbus slave.
		    		con = new TCPMasterConnection(addr);
		    		con.setPort(port);
		    		con.setTimeout(this.timeOut);
					con.connect();
					
					Map.Entry<LocalDateTime,TCPMasterConnection> newEntry = new AbstractMap.SimpleEntry<LocalDateTime,TCPMasterConnection>(LocalDateTime.now(), con);
					
					if (!(this.usedConnections.containsKey(key))){
						Stack<Map.Entry<LocalDateTime,TCPMasterConnection>> list = new Stack<Map.Entry<LocalDateTime,TCPMasterConnection>>();
						this.usedConnections.put(key, list);
					}

					// Insert the connection into the used connections.
					this.usedConnections.get(key).push(newEntry);
					
					logger.debug("connection established");
					
					return newEntry.getValue();
					
				} catch (Exception e) {
					logger.error("could not to connect with the modbus slave with ipaddress" + ipAddress + " port:" + Integer.toString(port));
					// e.printStackTrace();
					return null;
				}
				
			}
		} else {
			logger.debug("max modbusconnections: " + maxModbusConnections);
			return null;
		}
	}

	/**
	 * Get the delayed queue. This queue maintains those events that should be executed in the future.
	 * Synchronized method because it can be called by many thread at the same time. 
	 * @return a reference to the queue.
	 * 
	 */
	public synchronized BlockingQueue<DelayEvent> getDelayedQueue() {
		return this.delayedQueue;
	}
	
	/**
	 * This method removes all the events associated to the measured entity given as parameter
	 * @param measuredId identifier of the measured entity to delete.
	 * @return true if the method execute successfully, false otherwise.
	 */
	public synchronized boolean removeEntityEvents(Integer measuredId, MeasuredEntityType type) {
		
		// List with the list of current events.
		List<DelayEvent> currentEvents = new ArrayList<DelayEvent>();
		
		// This will be the list of elements to delete from current events.
		List<DelayEvent> removeEvents = new ArrayList<DelayEvent>();
		
		this.delayedQueue.drainTo(currentEvents);
		
		Iterator<DelayEvent> itr= currentEvents.iterator();
		while(itr.hasNext()){
			DelayEvent dt= (DelayEvent) itr.next();
			if ((dt.getEvent().getEntity().equals(measuredId)) 
					&& (dt.getEvent().getOwnerType().equals(type))){
				
				removeEvents.add(dt);
			}
		}
		
		for (DelayEvent event : removeEvents){
			currentEvents.remove(event);
		}
		
		// The list of events after being eliminated are added again to the list of delayed events.
		this.delayedQueue.addAll(currentEvents);
		
		return true;
	}
	
    /**
     * This method let a TCP modbus event process to inform that a connection being used can be released to another handler.
     * @param ipAddress ip address modbus slave
     * @param port      slave's port
     * @param con       connection being released.
     * @throws Exception
     */
    public synchronized void releaseModbusConnection(String ipAddress, int port, TCPMasterConnection con) throws Exception
    {
    	
    	String key = ipAddress + ":" + Integer.toString(port);
    	
    	if (this.usedConnections.containsKey(key))
    	{
    		boolean found= false;
    		Stack<Map.Entry<LocalDateTime,TCPMasterConnection>> list = this.usedConnections.get(key);
    		for (int index=0; index < list.size(); index++){
    			if (con.equals(list.get(index).getValue())){
    				Map.Entry<LocalDateTime,TCPMasterConnection> entryCon = list.get(index);
    				list.remove(index);

					if (!(this.availableConnections.containsKey(key))){
						Stack<Map.Entry<LocalDateTime,TCPMasterConnection>> listInsert = new Stack<Map.Entry<LocalDateTime,TCPMasterConnection>>();
						this.availableConnections.put(key, listInsert);
					}
					
					// Insert the connection into the available connections.
					this.availableConnections.get(key).push(entryCon);
					
					// The connection was found
					found= true;
					break;
    			}
    		}
    		
    		if (found == false){
    			logger.error("The connection for Ip Address was not found in Connection Container" + key);
    			throw new Exception("The connection for Ip Address was not found in Connection Container" + key);
    		}
    	} else {
    		logger.error("Ip Address Not found In Connection Container:" + key);
    		throw new Exception("Ip Address Not found In Connection Container:" + key);
    	}
    }

    /**
     * Delete an event from the event queue by its key. 
     *
     * @param key Event to delete it is deleted by comparing its key.
     */
    public synchronized boolean removeEvent(DelayEvent event) {
    	return this.delayedQueue.remove(event);
	}
    
    public synchronized boolean blockProcessingHandler(EventType type) {
    	
    	Integer oldNumHandlers = (Integer) this.numActiveHandlers.get(type);
    	Integer maximum = this.maximimActiveHandlers.get(type);
    	
    	if (maximum == null) {
    		// No Maximum has been specified
    	
	    	if (oldNumHandlers == null) {
	    		this.numActiveHandlers.put(type, new Integer(1));
	    	} else {
	    		this.numActiveHandlers.put(type, new Integer (oldNumHandlers.intValue()+1) );
	    	}
	    	
	    	return true;
	    	
    	} else {
    		if (maximum.intValue() == 0) {
    			// This kind of event cannot be processed.
    			return false;
    		} else {
    			// This kind of event can be processed with a limit
    			if (oldNumHandlers == null) {
    				// It has not been processed before, then insert the entry and return as reserved
    				this.numActiveHandlers.put(type, new Integer(1));
    				return true;
    			} else {
    				if (oldNumHandlers.intValue() + 1 <= maximum.intValue()) {
    					// we are within the limits  
    					this.numActiveHandlers.put(type, new Integer (oldNumHandlers.intValue()+1) );
    					return true;
    				} else {
    					// The maximum has been reached.
    					return false;
    				}	
    			}
    		}
    	}
    }
    
    public synchronized void releaseProcessingHandler(EventType type) {
    	
    	Integer oldNumHandlers = (Integer) this.numActiveHandlers.get(type);
    	
    	if (oldNumHandlers != null) {
    		this.numActiveHandlers.put(type, new Integer (oldNumHandlers.intValue()-1) );
    	}
    	
    	logger.debug("In release process" + oldNumHandlers);
    }
    
    public synchronized int getActiveProcessingHandlers(EventType type) {
    	Integer oldNumHandlers = (Integer) this.numActiveHandlers.get(type);
    	
    	if (oldNumHandlers == null) {
    		return 0;
    	} else {
    		return oldNumHandlers.intValue();
    	}
    }
    
    public synchronized void setProcessingLimit(EventType type, int value) {
    	
    	Integer maximum = this.maximimActiveHandlers.get(type);
    	if (maximum == null) {
    		this.maximimActiveHandlers.put(type, new Integer(value));
    	} else {
    		this.maximimActiveHandlers.replace(type, new Integer(value));
    	}
    }
    
    public synchronized int getProcessingLimit(EventType type) {
    	
    	Integer maximum = this.maximimActiveHandlers.get(type);
    	if (maximum == null) {
    		return numHandlers;
    	} else {
    		return maximum.intValue();
    	}
    }
    
    public synchronized String evntsToString() {
    	Iterator<DelayEvent> iter = this.getDelayedQueue().iterator();
    	
    	String result = ""; 
    	while (iter.hasNext()) {
    		result = result + " key:" + iter.next().getKey(); 
    	}
    	
    	return result;
    }
}
