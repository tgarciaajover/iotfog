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
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersResponse;
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
		Map<String, Object> dictionary = new HashMap<String, Object>();
		
		try {
			// Obtains the connection 
			TCPMasterConnection con = eventManager.getModbusConnection(event.getIpAddress(), event.getPort());
	
			ModbusTCPTransaction trans = null; //the transaction
			
			switch (event.getType())
			{
				case READ_DISCRETE: 
					ModBusTcpDiscreteDataInputEvent evt = (ModBusTcpDiscreteDataInputEvent) event;
					adapterManager = AdapterManager.getInstance(); 				
				    ReadInputDiscretesRequest req = null; //the request
					ReadInputDiscretesResponse res = null; //the response
			
					// Prepare the request
					req = new ReadInputDiscretesRequest(evt.getOffset(), evt.getCount());
					req.setUnitID(evt.getUid());
			
					// Prepare the transaction
					trans = new ModbusTCPTransaction(con);
					trans.setRequest(req);
					
					// Execute the transaction repeat times
					trans.execute();
					res = (ReadInputDiscretesResponse) trans.getResponse();
					byte byteRes[] = new byte[evt.getCount()];  
					for (int i = 0; i < evt.getCount(); i++){
						boolean bool = res.getDiscretes().getBit(i);
						byteRes[i] = (byte)(bool?1:0);
					}
										
					dictionary.put("IPAddress", event.getIpAddress());
					dictionary.put("UID", event.getUid());
					dictionary.put("Offset", evt.getOffset());
					dictionary.put("Count", evt.getCount());
					dictionary.put("Type", (Integer) event.getType().getValue());
					dictionary.put("Read", byteRes);
					
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

					ModBusTcpInputRegisterEvent evt2 = (ModBusTcpInputRegisterEvent) event;
					adapterManager = AdapterManager.getInstance(); 				
					ReadInputRegistersRequest req2 = null; //the request
					ReadInputRegistersResponse res2 = null; //the response
			
					// Prepare the request
					req2 = new ReadInputRegistersRequest(evt2.getOffset(), evt2.getCount());
					req2.setUnitID(evt2.getUid());
			
					// Prepare the transaction
					trans = new ModbusTCPTransaction(con);
					trans.setRequest(req2);
					
 				    trans.execute();
					res2 = (ReadInputRegistersResponse) trans.getResponse();
					
					dictionary.put("IPAddress", event.getIpAddress());
					dictionary.put("UID", event.getUid());
					dictionary.put("Offset", evt2.getOffset());
					dictionary.put("Count", evt2.getCount());
					dictionary.put("Type", (Integer) event.getType().getValue());
					dictionary.put("Read", res2.getMessage());
					
					Queueable obj2 = new Queueable(QueueType.MODBUS_DEV_MESSAGE, dictionary);
					adapterManager.getQueue().enqueue(6, obj2);
					
					// Insert again in the queue the event
					if (evt2.isRepeated()){
						long milliseconds = evt2.getMilliseconds();
						DelayEvent dEvent = new DelayEvent(event,milliseconds);
						retEvts.add(dEvent);
					}
					
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
