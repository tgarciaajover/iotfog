package com.advicetec.eventprocessor;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.MessageProcessor.MessageManager;
import com.advicetec.mpmcqueue.PriorityQueue;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;

/**
 * This class implements a switch in order to determine the class needed to process a particular event type.
 * 
 * In general it is waiting for new events in the queue, once it has an event ask for the type of event 
 * 	and based on that creates the corresponding processing. 
 * 
 * There are many handlers executed as threads and listening to the queue. 
 * 
 * @author Andres Marentes
 *
 */
public class EventHandler implements Runnable
{

	static Logger logger = LogManager.getLogger(EventHandler.class.getName());

	/**
	 * This queue is where we listen for new events. 
	 */
	private PriorityQueue<Queueable> queue;

	/**
	 * This queue is where we put those new events resulting from processing an event.
	 */
	private BlockingQueue<DelayEvent> delayQueue;
	
	/**
	 * Constructor for the class, it takes the two queues.
	 * @param queue  		queue where we listen for events
	 * @param delayedQueue	queue where we put new events
	 */
	public EventHandler(PriorityQueue<Queueable> queue, BlockingQueue<DelayEvent> delayedQueue) {
		super();
		this.queue = queue;
		this.delayQueue = delayedQueue;
	}

	public void repeat(Event evnt) throws InterruptedException {
		
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

	}
	
	public void runMeasuredEntityEvent(MeasuredEntityEvent measuEntyEvt) throws SQLException, InterruptedException {

		MeasuredEntityEventProcessor processor = new MeasuredEntityEventProcessor(measuEntyEvt);
		logger.debug("processing measuring entity event");
		
		List<DelayEvent> eventsToCreate = processor.process();
		
		logger.debug("Num events returned after processing:" + eventsToCreate.size());
		for ( int i=0; i < eventsToCreate.size(); i++){
			DelayEvent event = eventsToCreate.get(i);
			this.delayQueue.put(event);
		}
		
		repeat(measuEntyEvt);

	}
	
	public void runSchedAggregateEntityEvent(SchedAggregateEntityEvent schedAggrEntyEvt) throws SQLException, InterruptedException {

		SchedAggregateEntityEventProcessor processor = new SchedAggregateEntityEventProcessor(schedAggrEntyEvt);
		logger.info("processing sched_aggregate entity event");
		
		List<DelayEvent> eventsToCreate = processor.process();
		
		logger.info("Num events returned after processing:" + eventsToCreate.size());
		for ( int i=0; i < eventsToCreate.size(); i++){
			DelayEvent event = eventsToCreate.get(i);
			this.delayQueue.put(event);
		}
		
		repeat(schedAggrEntyEvt);

	}
	
	public void runDisplayEvent(DisplayEvent displayEvt) throws SQLException, InterruptedException {

		DisplayEventProcessor processor = new DisplayEventProcessor(displayEvt);
		logger.debug("processing display event");
		
		List<DelayEvent> eventsToCreate = processor.process();
		
		for ( int i=0; i < eventsToCreate.size(); i++){
			DelayEvent event = eventsToCreate.get(i);
			this.delayQueue.put(event);
		}							
		
		repeat(displayEvt);
	}
	
	public void runModbusReadEvent(ModBusTcpEvent modbusEvt) throws SQLException, InterruptedException {
		
		ModBusTcpProcessor processor = new ModBusTcpProcessor(modbusEvt);
		logger.debug("processing modbus event");
		logger.debug("Initial Num delayed enqueued elements is:" + this.delayQueue.size());
		List<DelayEvent> eventsToCreate = processor.process();
		for ( int i=0; i < eventsToCreate.size(); i++){
			DelayEvent event = eventsToCreate.get(i);
			this.delayQueue.put(event);
		}
		
		repeat(modbusEvt);
		
		logger.debug("The Num delayed enqueued elements is:" + this.delayQueue.size());
		
	}
	
	public void runAggregationEvent(AggregationEvent aggregationEvent) throws SQLException, InterruptedException {
		
		logger.debug("It is going to start to execute the OEE Aggregation");
		
		if (aggregationEvent.getType() == AggregationEventType.OEE)
		{
			OEEAggregationEventProcessor processor = new OEEAggregationEventProcessor(aggregationEvent);
			
			List<DelayEvent> eventsToCreate = processor.process();
			
			for ( int i=0; i < eventsToCreate.size(); i++){
				DelayEvent event = eventsToCreate.get(i);
				this.delayQueue.put(event);
			}
			logger.debug("Finished executing OEE aggregation - delayeEvents:" + eventsToCreate.size());
			
		} else {
			logger.error("This aggregation event type: " + 
							aggregationEvent.getType().getName() + 
								" can not be processed");
		}
		
		repeat(aggregationEvent);

	}
	
	public void runTestEvent(TestEvent testEvent) throws InterruptedException {

		// Sleep the thread by 10 milliseconds.
		Thread.sleep(50);
		logger.info("Finished running test Id: " + testEvent.getTestId());
		
		repeat(testEvent);

	}
	
	public void runPurgeFacadeEvent(PurgeFacadeCacheMapsEvent event) throws SQLException, InterruptedException {
		
		PurgeFacadeCacheMapsEventProcessor processor = new PurgeFacadeCacheMapsEventProcessor(event);
		List<DelayEvent> eventsToCreate = processor.process();
		
		for ( int i=0; i < eventsToCreate.size(); i++){
			DelayEvent newEvent = eventsToCreate.get(i);
			this.delayQueue.put(newEvent);
		}
		logger.debug("Finished executing Purge Facade Event - delayeEvents:" + eventsToCreate.size());
		
		repeat(event);
		
	}
	
	/**
	 * This is the main method of the handler, it waits for new events and processes them by creating the corresponding processor. 
	 * For now, we can process the following event types:
	 * 
	 *  	MEASURING_ENTITY_EVENT - Executes a behavior
	 *  	DISPLAY_EVENT		   - Shows a message in a display
	 *  	MODBUS_READ_EVENT      - Reads a sensor using the modbus protocol
	 *  	AGGREGATION_EVENT	   - Executes an aggregation.
	 */
	public void run() {

		try {

			while (true)
			{		

				Queueable obj = (Queueable) queue.pop();
				Event evnt = (Event) obj.getContent();

				logger.debug("elements in queue:" + queue.size()[6] );

				boolean reserved = EventManager.getInstance().blockProcessingHandler(evnt.getEvntType());

				if (reserved == false) {
					// if (EventManager.getInstance().getProcessingLimit(evnt.getEvntType()) != 0) { 
						// We have to enqueue the event because the maximum number of handers has been reached.
						// queue.enqueue(6, obj);
					//} else {
					//	logger.warn("The event type" + evnt.getEvntType().getName() + " cannot be processed by configuration- we remove the event from the queue" );
					// }

				} else {

					// It can process the event.	
					logger.debug("start to process event:" + evnt.getId() + " type:" + evnt.getEvntType().getName());


					if  (evnt.getEvntType() == EventType.MEASURING_ENTITY_EVENT)
					{				
						runMeasuredEntityEvent((MeasuredEntityEvent) evnt);

					} else if (evnt.getEvntType() == EventType.DISPLAY_EVENT) {

						runDisplayEvent( (DisplayEvent) evnt);

					} else if (evnt.getEvntType() == EventType.MODBUS_READ_EVENT) {

						runModbusReadEvent((ModBusTcpEvent) evnt);

					} else if (evnt.getEvntType() == EventType.AGGREGATION_EVENT ){

						runAggregationEvent((AggregationEvent) evnt);

					} else if (evnt.getEvntType() == EventType.TEST) {

						runTestEvent( (TestEvent) evnt);

					} else if (evnt.getEvntType() == EventType.PURGE_FACADE_MAPS){
						
						runPurgeFacadeEvent((PurgeFacadeCacheMapsEvent) evnt);
					} else if (evnt.getEvntType() == EventType.SCHED_AGGREGATION_EVENT){
						runSchedAggregateEntityEvent((SchedAggregateEntityEvent) evnt);
						
					}
					else {
						logger.error("This event cannot be processed" + evnt.getEvntType().getName());
					}


					// Release an event handler 
					EventManager.getInstance().releaseProcessingHandler(evnt.getEvntType());

					logger.debug("finish processing event:" + evnt.getId());
				}
			}

		} catch (InterruptedException e) {
			logger.error("Interruped Operation - Error:" + e.getMessage());
			e.printStackTrace();
		} catch (Exception e){
			logger.error("Exception:" + e.getMessage() );
			e.printStackTrace();
		}
		
		logger.info("Handler finished");
	}


}
