package com.advicetec.MessageProcessor;

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
	
	// This queue is to put the events that need to be executed right now.
	private PriorityQueue toQueue;
	
	// This queue is used to put the events that need to be scheduled for later execution.
	private BlockingQueue queue; 

	public MessageHandler(PriorityQueue fromQueue, PriorityQueue toQueue) {
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
							processor.process();

							// TODO: include priority parameter for the type of message. 
							// = toQueue.enqueue(7, um);
							
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
