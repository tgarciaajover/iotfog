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
	private BlockingQueue delayQueue;

	public EventHandler(PriorityQueue queue, BlockingQueue delayedQueue) {
		super();
		this.queue = queue;
		this.delayQueue = delayedQueue;		
	}

	public void run() {

		try {

			while (true)
			{

				int[] size = queue.size();
				String sizeStr = "";
				for (int i=0; i < size.length; i++){
					sizeStr = sizeStr + "i:" + i + "size:" + size[i] + ",";
				}
				logger.info("before event queue size" + sizeStr );

				Queueable obj = (Queueable) queue.pop();
				Event evnt = (Event) obj.getContent();


				size = queue.size();
				sizeStr = "";
				for (int i=0; i < size.length; i++){
					sizeStr = sizeStr + "," + i + ":" + size[i] + ",";
				}
				logger.info("after event queue size" + sizeStr );

				logger.info("start to process event:" + evnt.getId() + " type:" + evnt.getEvntType().getName());
				

				if  (evnt.getEvntType() == EventType.MEASURING_ENTITY_EVENT)
				{				
					MeasuredEntityEvent measuEntyEvt = (MeasuredEntityEvent) evnt;
					MeasuredEntityEventProcessor processor = new MeasuredEntityEventProcessor(measuEntyEvt);
					logger.info("processing measuring entity event");
					
					List<DelayEvent> eventsToCreate = processor.process();
					
					logger.info("Num events returned after processing:" + eventsToCreate.size());
					for ( int i=0; i < eventsToCreate.size(); i++){
						DelayEvent event = eventsToCreate.get(i);
						this.delayQueue.put(event);
					}							
					
				} else if (evnt.getEvntType() == EventType.DISPLAY_EVENT) {
					DisplayEvent displayEvt = (DisplayEvent) evnt;
					DisplayEventProcessor processor = new DisplayEventProcessor(displayEvt);
					logger.info("processing display event");
					List<DelayEvent> eventsToCreate = processor.process();
					for ( int i=0; i < eventsToCreate.size(); i++){
						DelayEvent event = eventsToCreate.get(i);
						this.delayQueue.put(event);
					}							

				} else if (evnt.getEvntType() == EventType.MODBUS_READ_EVENT) {
					ModBusTcpEvent modbusEvt = (ModBusTcpEvent) evnt;
					ModBusTcpProcessor processor = new ModBusTcpProcessor(modbusEvt);
					logger.debug("processing modbus event");
					logger.info("Initial Num delayed enqueued elements is:" + this.delayQueue.size());
					List<DelayEvent> eventsToCreate = processor.process();
					for ( int i=0; i < eventsToCreate.size(); i++){
						DelayEvent event = eventsToCreate.get(i);
						this.delayQueue.put(event);
					}
					logger.info("The Num delayed enqueued elements is:" + this.delayQueue.size());
				} else {
					logger.error("This event cannot be processed" + evnt.getEvntType().getName());
				}
				
				logger.info("finish processing event" + evnt.getId());
			}

		} catch (InterruptedException e) {
			logger.error("Interruped Operation - Error:" + e.getMessage());
			e.printStackTrace();
		} catch (Exception e){
			logger.error("Exception:" + e.getMessage() );
		}

	}


}
