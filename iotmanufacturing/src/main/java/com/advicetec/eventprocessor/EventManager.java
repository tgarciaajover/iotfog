package com.advicetec.eventprocessor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.DelayQueueConsumer;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.core.Manager;
import com.advicetec.language.behavior.BehaviorDefPhase;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;


public class EventManager extends Manager
{

	static Logger logger = LogManager.getLogger(EventManager.class.getName());
	
	private static EventManager instance=null;
	private static ConfigurationManager confManager = null;
	private BlockingQueue delayedQueue = null;
	private int maxModbusConnections = 0;
	private int timeOut = 0; 
	
	
	// This hashmap contains the available connections for the ipadress in the key of the hashmap. 
	private Map<String, Stack<Map.Entry<LocalDateTime,TCPMasterConnection>> > availableConnections = null;
	
	// This hashmap contains the used connections for the ipadress in the key of the hashmap.
	private Map<String, Stack<Map.Entry<LocalDateTime,TCPMasterConnection>>> usedConnections = null;
		
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
		this.availableConnections = new HashMap<String, Stack<Map.Entry<LocalDateTime,TCPMasterConnection>>>();
		
		// This list contains the connections being used by some handler
		this.usedConnections = new HashMap<String, Stack<Map.Entry<LocalDateTime,TCPMasterConnection>>>();
		
		// Maximum number of modbus connections.
		String strMaxModbusConnections = getProperty("MaxModbusConnections");
		strMaxModbusConnections = strMaxModbusConnections.replaceAll("\\s","");
		maxModbusConnections = Integer.valueOf(strMaxModbusConnections); 
		
		String strTimeOut = getProperty("TimeOut");
		strTimeOut = strTimeOut.replaceAll("\\s","");
		timeOut = Integer.valueOf(strTimeOut);
		
		logger.info("timeout given:" + String.valueOf(timeOut));
		
	}	

	public void run() 
	{
		logger.info("Starting Event Manager run");
		
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
		
		logger.info("Ending Event Manager run");
	}	

	private LocalDateTime getActiveModbusConnection(Map.Entry<LocalDateTime,TCPMasterConnection> con) throws Exception
	{
		
		logger.info("in getActiveModbusConnection");
		
		LocalDateTime start = con.getKey();
		if (start.plusNanos(con.getValue().getTimeout()* 1000000).isBefore(LocalDateTime.now())){
			con.getValue().close();
			con.getValue().connect();
			return LocalDateTime.now();
		} else {
			return con.getKey();
		}
	}
	
	public synchronized TCPMasterConnection getModbusConnection(String ipAddress, int port) throws UnknownHostException
	{
		logger.info("in getModbusConnection  address: " + ipAddress + " port: " + port  );
		
		int usedCon = 0;
		if (this.availableConnections.get(ipAddress) != null)
			usedCon = this.availableConnections.get(ipAddress).size();
			
		
		int avilCon = 0;
		if (this.usedConnections.get(ipAddress) != null)
			avilCon = this.usedConnections.get(ipAddress).size();
		
		if (usedCon + avilCon <= maxModbusConnections){
			if (avilCon > 0)
			{
				
				logger.info("There are avail connections - availCon: " + avilCon + " usedConnection: " + String.valueOf((usedCon + avilCon)));
				
				// Remove the connection from available connections
				Map.Entry<LocalDateTime,TCPMasterConnection> ret = availableConnections.get(ipAddress).pop();
				
				try{
					// Update the connection (reconnect or maintain the connection).
					LocalDateTime start = getActiveModbusConnection(ret);
					
					// Create the entry.
					Map.Entry<LocalDateTime,TCPMasterConnection> newEntry = new AbstractMap.SimpleEntry<LocalDateTime,TCPMasterConnection>(start, ret.getValue()); 
					
					// Verifies that there exists a node for the ipadddres
					if (!(this.usedConnections.containsKey(ipAddress))){
						Stack<Map.Entry<LocalDateTime,TCPMasterConnection>> list = new Stack<Map.Entry<LocalDateTime,TCPMasterConnection>>();
						this.usedConnections.put(ipAddress, list);
					}
	
					// Insert the connection into the used connections.
					this.usedConnections.get(ipAddress).push(newEntry);
					
					logger.info("here I am");
					
					// return the connection.
					return newEntry.getValue();

				} catch (Exception e) {
					logger.error("enable to connect with the modbus slave with ipaddress" + ipAddress + " port:" + Integer.toString(port));
					e.printStackTrace();
					return null;
				}
				
			} else {
				
				logger.info("There are not avail connections - availCon: " + avilCon);
				
				// Create a new connection and return it.
	    		TCPMasterConnection con = null; //the connection
	    		
	    		// Create the address object from the string.
	    		InetAddress addr =  InetAddress.getByName(ipAddress); //the slave's address
	    		try {
	    			
	    			logger.info("Ipaddress: " + addr.toString());
	    			
	    			// Creates the connection to modbus slave.
		    		con = new TCPMasterConnection(addr);
		    		con.setPort(port);
		    		con.setTimeout(this.timeOut);
					con.connect();
					
					Map.Entry<LocalDateTime,TCPMasterConnection> newEntry = new AbstractMap.SimpleEntry<LocalDateTime,TCPMasterConnection>(LocalDateTime.now(), con);
					
					if (!(this.usedConnections.containsKey(ipAddress))){
						Stack<Map.Entry<LocalDateTime,TCPMasterConnection>> list = new Stack<Map.Entry<LocalDateTime,TCPMasterConnection>>();
						this.usedConnections.put(ipAddress, list);
					}

					// Insert the connection into the used connections.
					this.usedConnections.get(ipAddress).push(newEntry);
					
					logger.info("connection established");
					
					return newEntry.getValue();
					
				} catch (Exception e) {
					logger.error("unenable to connect with the modbus slave with ipaddress" + ipAddress + " port:" + Integer.toString(port));
					e.printStackTrace();
					return null;
				}
				
			}
		} else {
			logger.info("max modbusconnections: " + maxModbusConnections);
			return null;
		}
	}

    public synchronized void releaseModbusConnection(String ipAddress, TCPMasterConnection con) throws Exception
    {
    	if (this.usedConnections.containsKey(ipAddress))
    	{
    		boolean found= false;
    		Stack<Map.Entry<LocalDateTime,TCPMasterConnection>> list = this.usedConnections.get(ipAddress);
    		for (int index=0; index < list.size(); index++){
    			if (con.equals(list.get(index).getValue())){
    				Map.Entry<LocalDateTime,TCPMasterConnection> entryCon = list.get(index);
    				list.remove(index);

					if (!(this.availableConnections.containsKey(ipAddress))){
						Stack<Map.Entry<LocalDateTime,TCPMasterConnection>> listInsert = new Stack<Map.Entry<LocalDateTime,TCPMasterConnection>>();
						this.availableConnections.put(ipAddress, listInsert);
					}
					
					// Insert the connection into the used connections.
					this.usedConnections.get(ipAddress).push(entryCon);
					
					// The connection was found
					found= true;
					break;
    			}
    		}
    		
    		if (found == false){
    			logger.error("The connection for Ip Address was not found in Connection Container" + ipAddress);
    			throw new Exception("The connection for Ip Address was not found in Connection Container" + ipAddress);
    		}
    	} else {
    		logger.error("Ip Address Not found In Connection Container:" + ipAddress);
    		throw new Exception("Ip Address Not found In Connection Container:" + ipAddress);
    	}
    }
}
