package com.advicetec.MessageProcessor;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.advicetec.mpmcqueue.PriorityQueue;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;
import com.advicetec.persistence.StatusStore;

/**
 * This class is in charge of process the semantic transformation and
 * create a list of Attributes which later will queue to the Event Processor.
 * 
 * @author user
 *
 */
public class MessageHandler implements Runnable
{

	private PriorityQueue fromQueue;
	
	// This queue is to put the events.
	private BlockingQueue toQueue;
	

	public MessageHandler(PriorityQueue fromQueue, BlockingQueue toQueue) {
		super();
		this.fromQueue = fromQueue;
		this.toQueue = toQueue;
	}

	public void run() {

		try {

			while (true)
			{
				Queueable obj = (Queueable) fromQueue.pop();
				
				if (obj.getType() == QueueType.UNIFIED_MESSAGE)
				{
					UnifiedMessage um = (UnifiedMessage) obj.getContent();
					
					switch (um.getType())
					{
						case SAMPLE:
							SampleMessage sample = (SampleMessage) um;
							SampleProcessor processor = new SampleProcessor(sample);
							List<DelayEvent> eventsToCreate = processor.process();	
							for ( int i=0; i < eventsToCreate.size(); i++){
								DelayEvent event = eventsToCreate.get(i);
								this.toQueue.put(event);
							}							
							break;
												
						case BROKER_MESSAGE:
							break;
						
						case INVALID:
							break;
					}
					
				}		
				
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	
}
