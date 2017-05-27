package com.advicetec.eventprocessor;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.core.Processor;
import com.advicetec.monitorAdapter.AdapterManager;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.ReadInputDiscretesRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputDiscretesResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;

public class ModBusTcpProcessor implements Processor {

	static Logger logger = LogManager.getLogger(ModBusTcpProcessor.class.getName());
	ModBusTcpEvent event;
	AdapterManager adapterManager=null;
	EventManager eventManager=null;

	public ModBusTcpProcessor(ModBusTcpEvent event) {
		super();
		this.event = event;
	}
	
	
	@Override
	public List<DelayEvent> process() throws SQLException {

		eventManager = EventManager.getInstance();
		ArrayList<DelayEvent> retEvts = new ArrayList<DelayEvent>();
		
		try {
			// Obtains the connection 
			TCPMasterConnection con = eventManager.getModbusConnection(event.getIpAddress(), event.getPort());
	
			ModbusTCPTransaction trans = null; //the transaction
			
			switch (event.getType())
			{
				case READ_DISCRETE: 
					ModBusTcpDiscreteDataInputEvent evt = (ModBusTcpDiscreteDataInputEvent) event;
					adapterManager = AdapterManager.getInstance(); 				
					String resDiscrete[] = new String[evt.getRepeat()];
				    ReadInputDiscretesRequest req = null; //the request
					ReadInputDiscretesResponse res = null; //the response
			
					// Prepare the request
					req = new ReadInputDiscretesRequest(evt.getOffset(), evt.getCount());
					req.setUnitID(evt.getUid());
			
					// Prepare the transaction
					trans = new ModbusTCPTransaction(con);
					trans.setRequest(req);
					
					// Execute the transaction repeat times
					int k = 0;
					do {
					  trans.execute();
					  res = (ReadInputDiscretesResponse) trans.getResponse();
					  resDiscrete[k] = res.getDiscretes().toString();
					  k++;
					} while (k < evt.getRepeat());
					
					Map<String, Object> dictionary = new HashMap<String, Object>();
					
					dictionary.put("IPAddress", event.getIpAddress());
					dictionary.put("UID", event.getUid());
					dictionary.put("Type", (Integer)event.getType().getValue());
					dictionary.put("Read", resDiscrete);
					
					Queueable obj = new Queueable(QueueType.MODBUS_DEV_MESSAGE, dictionary);
					adapterManager.getQueue().enqueue(6, obj);
					
					// Insert again in the queue the event
					if (evt.isRepeated()){
						long milliseconds = evt.getMilliseconds();
						DelayEvent dEvent = new DelayEvent(event,milliseconds);
						retEvts.add(dEvent);
					}
					
					break;
				case READ_REGISTER:
					break;
				case WRITE_DISCRETE:
						// TODO: we are not writing anything.
					break;
				case WRITE_REGISTER:
	   				   // TODO: we are not writing anything.
					break;
				case INVALID:
					break;
				 			
			} 
			
			eventManager.releaseModbusConnection(event.getIpAddress(), con);
			
			return null;
		} catch (UnknownHostException e) {
			logger.error("Error creating the connection with the modbus slave for ipaddress:" + event.getIpAddress() + " port:" + Integer.toString(event.getPort()));
		} catch (Exception e){
			logger.error("Error releasing the connection with the modbus slave for ipaddress:" + event.getIpAddress() + " port:" + Integer.toString(event.getPort()));
		}
		
		return retEvts;
		
	}

}
