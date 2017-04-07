package com.advicetec.MessageProcessor;

import com.advicetec.mpmcqueue.PriorityQueue;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;

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
	private PriorityQueue toQueue;

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
							break;
												
						case BROKER_MESSAGE:
							break;
						
						case INVALID:
							break;
					}
					
					// TODO: include priority parameter for the type of message. 
					// = toQueue.enqueue(7, um);
				}		
				
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	
}
