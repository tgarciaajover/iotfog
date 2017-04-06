package com.advicetec.monitorAdapter;

import com.advicetec.MessageProcessor.UnifiedMessage;
import com.advicetec.mpmcqueue.PriorityQueue;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;

/**
 * 
 * @author user
 *
 */
public class AdapterHandler implements Runnable 
{

	private PriorityQueue fromQueue;
	private PriorityQueue toQueue;

	public AdapterHandler(PriorityQueue fromQueue, PriorityQueue toQueue) {
		super();
		this.fromQueue = fromQueue;
		this.toQueue = toQueue;
	}

	public void run() {

		try {

			while (true)
			{
				Queueable message = (Queueable) fromQueue.pop();
				
				if (message.getType() == QueueType.MQTT_DEV_MESSAGE)
				{
					Mqtt2UnifiedMessage mq2Um = new Mqtt2UnifiedMessage();
					UnifiedMessage um = mq2Um.getUnifiedMessage();
					// TODO: include priority parameter for the type of message. 
					toQueue.enqueue(7, um);
				}		
				
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	
}
