package com.advicetec.monitorAdapter;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;

import com.advicetec.MessageProcessor.UnifiedMessage;
import com.advicetec.mpmcqueue.PriorityQueue;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;

/**
 * This class implements a queue handler for the Adapter Manager.
 * It processes the message that comes from the adapter, builts a Unified 
 * Message and queue it to the next queue.
 * It uses two queues, one to receive (consume), and another to push (produce) 
 * the results.
 * <p>
 * The <code>run</code> method pops elements from the origin queue, then identifies the
 * type of incoming input as MQTT or Modbus; then translate it to a Unified
 * Message and queue the message in the output queue.
 * 
 * @author advicetec
 * @see AdapterManager
 * @see UnifiedMessage
 * @see Mqtt2UnifiedMessage
 * @see Modbus2UnifiedMessage
 */
public class AdapterHandler implements Runnable 
{
	private final static int DEFAULT_PRIORITY = 6;
	static Logger logger = LogManager.getLogger(AdapterHandler.class.getName());

	// origin and destination queues.
	private PriorityQueue<Queueable> fromQueue;
	private PriorityQueue<Queueable> toQueue;

	/**
	 * Constructor.
	 * @param fromQueue origin queue. 
	 * @param toQueue destination queue.
	 */
	public AdapterHandler(PriorityQueue<Queueable> fromQueue, PriorityQueue<Queueable> toQueue) {
		super();
		this.fromQueue = fromQueue;
		this.toQueue = toQueue;
	}


	public void run() {
		try {
			while (true)
			{
				Queueable queueable = (Queueable) fromQueue.pop();
				logger.debug("a queueable object was found");

				// queable objet type MQTT
				if (queueable.getType() == QueueType.MQTT_DEV_MESSAGE)
				{
					// translate MQTT to Unified Message
					MqttPublish message = (MqttPublish) queueable.getContent(); 
					Mqtt2UnifiedMessage mq2Um = new Mqtt2UnifiedMessage(message);
					List<UnifiedMessage> ums;
					try 
					{
						ums = mq2Um.getUnifiedMessage();

						if (ums != null){
							for(UnifiedMessage um : ums){
								Queueable obj = new Queueable(QueueType.UNIFIED_MESSAGE, um);
								toQueue.enqueue(DEFAULT_PRIORITY, obj);
							}
						} 

					} catch (ClassNotFoundException | NoSuchMethodException
							| SecurityException | InstantiationException
							| IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {

						e.printStackTrace();
						logger.error("cannot queue unified messages from :" + message.getTopicName() );
					}
					// TODO: include priority parameter for the type of message. 
				}

				
				// queueable object with type MODBUS Message
				if(queueable.getType() == QueueType.MODBUS_DEV_MESSAGE ){
					// creates a dictionary (map) from the origin queue. 
					@SuppressWarnings("unchecked")
					Map<String, Object> dictionary = (Map<String, Object>) queueable.getContent();
					Modbus2UnifiedMessage mod2Um = new	Modbus2UnifiedMessage(dictionary);
					List<UnifiedMessage> ums;
					// tries to queue the Unified Message into destination queue
					// sets the DEFAULT_PRITORY into destination queue
					try {
						ums = mod2Um.getUnifiedMessage();
						if (ums != null){
							for(UnifiedMessage um : ums){
								Queueable obj = new Queueable(QueueType.UNIFIED_MESSAGE, um);
								toQueue.enqueue(DEFAULT_PRIORITY, obj);
							}
						}
					} catch ( Exception e) {
						logger.error("cannot queue unified messages from: "
								+ dictionary.get("IPAddress") + dictionary.get("UID") );
						e.printStackTrace();
						
					} 
				}
				
				if (queueable.getType() == QueueType.MODBUS_ERR_MESSAGE) {
					
					logger.info("Processing Modbus Error Message");
					
					Map<String, Object> dictionary = (Map<String, Object>) queueable.getContent();
					Modbus2UnifiedMessage mod2Um = new	Modbus2UnifiedMessage(dictionary);

					List<UnifiedMessage> ums;
					// tries to queue the Unified Message into destination queue
					// sets the DEFAULT_PRITORY into destination queue
					try {
						ums = mod2Um.getUnifiedMessage();
						if (ums != null){
							for(UnifiedMessage um : ums){
								Queueable obj = new Queueable(QueueType.UNIFIED_MESSAGE, um);
								toQueue.enqueue(DEFAULT_PRIORITY, obj);
							}
						}
					} catch ( Exception e) {
						logger.error("cannot queue unified messages from: "
								+ dictionary.get("IPAddress") + dictionary.get("UID") );
						e.printStackTrace();
						
					} 

				}
				
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


}
