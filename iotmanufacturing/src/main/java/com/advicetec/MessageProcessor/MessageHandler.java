package com.advicetec.MessageProcessor;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.mpmcqueue.PriorityQueue;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;

/**
 * This class implements a queue handler for the Message Manager.
 * This class is in charge of process the semantic transformation and
 * create a list of Attributes which later will queue to the Event Processor.
 * 
 * It uses two queues, one to receive messages,  <i>samples</i>, in Unified
 * Message format; then the message queued to be attended by a specialized 
 * processor, e.g. a <code>SampleProcessor</code>.
 * Also, it creates a future event if the processor requires it.
 * 
 * @author advicetec
 * @see UnifiedMessage
 * @see SampleMessage
 * @see SampleProcessor
 * @see DelayEvent
 * 
 */
public class MessageHandler implements Runnable
{

	static final Logger logger = LogManager.getLogger(MessageHandler.class.getName()); 
	/**
	 * Origin queue in Unified Message format.
	 */
	private PriorityQueue fromQueue;

	/**
	 * queue that holds events.
	 */
	private BlockingQueue toQueue;


	public MessageHandler(PriorityQueue fromQueue, BlockingQueue toQueue) {
		super();
		this.fromQueue = fromQueue;
		this.toQueue = toQueue;
	}

	public void run() {
		try {
			while (true){
				
				logger.debug("Elements in the message adapter:" + fromQueue.size()[6]);
				Queueable obj = (Queueable) fromQueue.pop();

				// interprets the unified message
				if (obj.getType() == QueueType.UNIFIED_MESSAGE)
				{
					UnifiedMessage um = (UnifiedMessage) obj.getContent();

					switch (um.getType())
					{
					// process a message of SAMPLE type
					case SAMPLE:
						SampleMessage sample = (SampleMessage) um;
						SampleProcessor processor = new SampleProcessor(sample);
						// get a list of delayed events related to this sample message.
						List<DelayEvent> eventsToCreate = processor.process();	
						// creation of delayed events
						for ( int i=0; i < eventsToCreate.size(); i++){
							DelayEvent event = eventsToCreate.get(i);
							logger.debug("Event key to search:" + event.getKey());
							// queues the event if  
							// it does not previously exists
							if (MessageManager.getInstance().
									existDelayEventType(event.getKey()) == false){
								this.toQueue.put(event);
								MessageManager.getInstance().
								addDelayEventType(event.getKey());
							} else {
								logger.debug("event of type: "
										+ event.getEvent().getEvntType().getName() +
										"already exists in the delayed queue - key:" + 
										event.getKey() );
							}
						}							
						break;
						
					case ERROR_SAMPLE:
						logger.info("Processing error sample");
						
						MeasuringErrorMessage message = (MeasuringErrorMessage) um;
						ErrorMessageSampleProcessor processor2 = new ErrorMessageSampleProcessor(message);
						
						List<DelayEvent> eventsToCreate2 = processor2.process();	

						for ( int i=0; i < eventsToCreate2.size(); i++){
							DelayEvent event = eventsToCreate2.get(i);
							logger.debug("Event key to search:" + event.getKey());
							// queues the event if  
							// it does not previously exists
							if (MessageManager.getInstance().
									existDelayEventType(event.getKey()) == false){
								this.toQueue.put(event);
								MessageManager.getInstance().
								addDelayEventType(event.getKey());
							} else {
								logger.debug("event of type: "
										+ event.getEvent().getEvntType().getName() +
										"already exists in the delayed queue - key:" + 
										event.getKey() );
							}
						}							

						break;
					
					case BROKER_MESSAGE:
						break;

					case INVALID:
						break;
					
					default:
						logger.error("Undefined Message. Cannot be processed.");
					}

				}		

			}

		} catch (InterruptedException e) {
			logger.error("The MessageHandler process was interrupted!");
			e.printStackTrace();
		} catch (SQLException e){
			logger.error("Container error, we cannot continue");
		}

	}
}
