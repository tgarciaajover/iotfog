package com.advicetec.eventprocessor;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.language.behavior.BehaviorInterpreter;
import com.advicetec.mpmcqueue.PriorityQueue;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;

public class EventHandler implements Runnable
{

	static Logger logger = LogManager.getLogger(EventHandler.class.getName());
	
	private PriorityQueue queue;

	// This queue is to put the events.
	private BlockingQueue toQueue;
	
	public EventHandler(PriorityQueue queue, BlockingQueue toQueue) {
		super();
		this.queue = queue;
		this.toQueue = toQueue;		
	}

	public void run() {

		try {

			while (true)
			{
				Queueable obj = (Queueable) queue.pop();
				Event evnt = (Event) obj.getContent();
				
				if  (evnt.getEvntType() == EventType.MEASURING_ENTITY_EVENT)
				{				
					    MeasuredEntityEvent measuEntyEvt = (MeasuredEntityEvent) evnt;
					    MeasuredEntityEventProcessor processor = new MeasuredEntityEventProcessor(measuEntyEvt);
					    logger.debug("processing measuring entity event");
					    List<DelayEvent> eventsToCreate = processor.process();
						for ( int i=0; i < eventsToCreate.size(); i++){
							DelayEvent event = eventsToCreate.get(i);
							this.toQueue.put(event);
						}							
					    
					    break;
				} else if (evnt.getEvntType() == EventType.DISPLAY_EVENT) {
						DisplayEvent displayEvt = (DisplayEvent) evnt;
						DisplayEventProcessor processor = new DisplayEventProcessor(displayEvt);
						logger.debug("processing display event");
						List<DelayEvent> eventsToCreate = processor.process();
						for ( int i=0; i < eventsToCreate.size(); i++){
							DelayEvent event = eventsToCreate.get(i);
							this.toQueue.put(event);
						}							
						
						break;
				} else if (evnt.getEvntType() == EventType.MODBUS_READ_EVENT) {
					    ModBusTcpEvent modbusEvt = (ModBusTcpEvent) evnt;
					    ModBusTcpProcessor processor = new ModBusTcpProcessor(modbusEvt);
					    logger.debug("processing modbus event");
						List<DelayEvent> eventsToCreate = processor.process();
						for ( int i=0; i < eventsToCreate.size(); i++){
							DelayEvent event = eventsToCreate.get(i);
							this.toQueue.put(event);
						}					    
				} else {
					logger.debug("This event cannot be processed" + evnt.getEvntType().getName());
				}
				
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (SQLException e){
			System.err.println("Container error, we cannot continue");
		}

	}

	
}
