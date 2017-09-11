package com.advicetec.MessageProcessor;

import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.eventprocessor.EventManager;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;

/**
 * The Message Handler queues the Delayed Events into a special queue.
 * After expiring the time of a Delayed Event, the event is processed by this 
 * consumer object.
 * This consumer pops the Event from the queue and returns it to the 
 * <code>EventManager</code> to be processed.
 * <p>
 * This class implements the Runnable interface to be threaded.
 * The <code>run</code> method takes events out from the Delayed Queue,
 * then re-processes them by sending their references to the EventManager.
 * 
 * @author advicetec
 * @see EventManager
 * @see DelayEvent
 */
public class DelayQueueConsumer implements Runnable 
{
	static Logger logger = LogManager.getLogger(DelayQueueConsumer.class.getName());
	
	private final static int DEFAULT_PRIORITY = 6;
	
	private String name;
	/**
	 * Queue with Delayed Events.
	 */
    private BlockingQueue queue;
    
    private EventManager eventManager;

	public DelayQueueConsumer(String name, BlockingQueue queue) 
	{
		super();
		this.name = name;
		this.queue = queue;
		this.eventManager = EventManager.getInstance();
	}
	
	public void run() 
	{
		logger.info("The DelayQueueConsumer is going to run");
		while (true) {
			try {
				// Take elements out from the DelayQueue object.
				DelayEvent object = (DelayEvent) queue.take();
				logger.debug("passing event:" + object.getId() + 
						" startdttm:" + object.getStartTime() +  
						" event type:" + object.getEvent().getEvntType().getName() + 
						" event info:" + object.getEvent().toString());
				// gets the event and queues it to be reprocessed by the EventManager
				Queueable obj = new Queueable(QueueType.EVENT, object.getEvent());
				eventManager.getQueue().enqueue(DEFAULT_PRIORITY,obj);
				
				// gets the number of elements on each queue.
				int[] size = eventManager.getQueue().size();
				String sizeStr = "";
				for (int i=0; i < size.length; i++){
					sizeStr = sizeStr + "i:" + i + "size:" + size[i] + ",";
				}		
				logger.debug("The DelayQueueConsumer enqueue other element - number of events:" + sizeStr);
		
			} catch (InterruptedException e) {
				logger.error("DelayConsumer is interrupted! "+ e.getMessage());
				e.printStackTrace();
			}
		}
	}

}