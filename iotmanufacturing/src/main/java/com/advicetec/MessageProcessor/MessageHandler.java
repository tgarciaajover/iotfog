package com.advicetec.MessageProcessor;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	static final Logger logger = LogManager.getLogger(MessageHandler.class.getName()); 
	
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
								logger.info("Event key to search:" + event.getKey());
								if (MessageManager.getInstance().existDelayEventType(event.getKey()) == false){
									this.toQueue.put(event);
									MessageManager.getInstance().addDelayEventType(event.getKey());
								} else {
									logger.info("event of type: "+ event.getEvent().getEvntType().getName() + " already exists in the delayed queue - key:" + event.getKey() );
								}
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
		} catch (SQLException e){
			System.err.println("Container error, we cannot continue");
		}

	}
	
	
}
