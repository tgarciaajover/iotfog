package com.advicetec.MessageProcessor;

import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.eventprocessor.EventManager;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;

public class DelayQueueConsumer implements Runnable 
{
	
	static Logger logger = LogManager.getLogger(DelayQueueConsumer.class.getName());
	
	private String name;
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
				logger.info("passing event:" + object.getId() + " startdttm:" + object.getStartTime() +  " event type:" + object.getEvent().getEvntType().getName() + " event info:" + object.getEvent().toString());
				MessageManager.getInstance().removeDelayEventType(object.getKey());
				Queueable obj = new Queueable(QueueType.EVENT, object.getEvent());
				// TODO : define the priority
				eventManager.getQueue().enqueue(6,obj);
				
				int[] size = eventManager.getQueue().size();
				String sizeStr = "";
				for (int i=0; i < size.length; i++){
					sizeStr = sizeStr + "i:" + i + "size:" + size[i] + ",";
				}
				
				logger.info("The DelayQueueConsumer enqueue other element - number of events:" + sizeStr);
				
				
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}

}