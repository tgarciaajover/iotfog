package com.advicetec.eventprocessor;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.MessageProcessor.MessageManager;
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
				logger.debug("before event queue size" + sizeStr );

				Queueable obj = (Queueable) queue.pop();
				Event evnt = (Event) obj.getContent();


				size = queue.size();
				sizeStr = "";
				for (int i=0; i < size.length; i++){
					sizeStr = sizeStr + "," + i + ":" + size[i] + ",";
				}
				logger.debug("after event queue size" + sizeStr );

				logger.debug("start to process event:" + evnt.getId() + " type:" + evnt.getEvntType().getName());
				

				if  (evnt.getEvntType() == EventType.MEASURING_ENTITY_EVENT)
				{				
					MeasuredEntityEvent measuEntyEvt = (MeasuredEntityEvent) evnt;
					MeasuredEntityEventProcessor processor = new MeasuredEntityEventProcessor(measuEntyEvt);
					logger.debug("processing measuring entity event");
					
					List<DelayEvent> eventsToCreate = processor.process();
					
					logger.debug("Num events returned after processing:" + eventsToCreate.size());
					for ( int i=0; i < eventsToCreate.size(); i++){
						DelayEvent event = eventsToCreate.get(i);
						this.delayQueue.put(event);
					}							
					
				} else if (evnt.getEvntType() == EventType.DISPLAY_EVENT) {
					DisplayEvent displayEvt = (DisplayEvent) evnt;
					DisplayEventProcessor processor = new DisplayEventProcessor(displayEvt);
					logger.debug("processing display event");
					List<DelayEvent> eventsToCreate = processor.process();
					for ( int i=0; i < eventsToCreate.size(); i++){
						DelayEvent event = eventsToCreate.get(i);
						this.delayQueue.put(event);
					}							

				} else if (evnt.getEvntType() == EventType.MODBUS_READ_EVENT) {
					ModBusTcpEvent modbusEvt = (ModBusTcpEvent) evnt;
					ModBusTcpProcessor processor = new ModBusTcpProcessor(modbusEvt);
					logger.debug("processing modbus event");
					logger.debug("Initial Num delayed enqueued elements is:" + this.delayQueue.size());
					List<DelayEvent> eventsToCreate = processor.process();
					for ( int i=0; i < eventsToCreate.size(); i++){
						DelayEvent event = eventsToCreate.get(i);
						this.delayQueue.put(event);
					}
					logger.debug("The Num delayed enqueued elements is:" + this.delayQueue.size());
				} else if (evnt.getEvntType() == EventType.AGGREGATION_EVENT ){
					AggregationEvent aggregationEvent = (AggregationEvent) evnt;
					
					if (aggregationEvent.getType() == AggregationEventType.OEE){
						logger.debug("It is going to start to execute the OEE Aggregation");
						OEEAggregationEventProcessor processor = new OEEAggregationEventProcessor(aggregationEvent);
						List<DelayEvent> eventsToCreate = processor.process();
						for ( int i=0; i < eventsToCreate.size(); i++){
							DelayEvent event = eventsToCreate.get(i);
							this.delayQueue.put(event);
						}
						logger.debug("The num of new delayed elements enqueued is:" + eventsToCreate.size());
						
					} else {
						logger.error("This aggregation event type: " + 
										aggregationEvent.getType().getName() + 
											" can not be processed");
					}
					
					
				} else {
					logger.error("This event cannot be processed" + evnt.getEvntType().getName());
				}

				// Insert again in the queue the event
				if (evnt.isRepeated()){
					long milliseconds = evnt.getMilliseconds();
					DelayEvent dEvent = new DelayEvent(evnt,milliseconds);
					this.delayQueue.put(dEvent);
				} else if ( evnt.getEvntType() == EventType.AGGREGATION_EVENT ) {
					
					long seconds = ((AggregationEvent) evnt).getSecondsToNextExecution();
					
					logger.debug("Next Recurrence to occur in: " + seconds + " seconds");
					
					DelayEvent dEvent = new DelayEvent(evnt,seconds*1000);
					this.delayQueue.put(dEvent);

				} else {
					// remove the event from the delay event list.
					MessageManager.getInstance().removeDelayEventType(evnt.getKey());
				}
				
				logger.debug("finish processing event" + evnt.getId());
			}

		} catch (InterruptedException e) {
			logger.error("Interruped Operation - Error:" + e.getMessage());
			e.printStackTrace();
		} catch (Exception e){
			logger.error("Exception:" + e.getMessage() );
		}

	}


}
