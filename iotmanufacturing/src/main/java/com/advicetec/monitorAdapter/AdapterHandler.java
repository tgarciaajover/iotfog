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
 * 
 * @author user
 *
 */
public class AdapterHandler implements Runnable 
{

	static Logger logger = LogManager.getLogger(AdapterHandler.class.getName());

	private PriorityQueue<Queueable> fromQueue;
	private PriorityQueue<Queueable> toQueue;

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

				// queable objet type mqtt
				if (queueable.getType() == QueueType.MQTT_DEV_MESSAGE)
				{

					MqttPublish message = (MqttPublish) queueable.getContent(); 
					Mqtt2UnifiedMessage mq2Um = new Mqtt2UnifiedMessage(message);
					List<UnifiedMessage> ums;
					try 
					{
						ums = mq2Um.getUnifiedMessage();

						if (ums != null){
							//TODO: to define the priority for the message.
							for(UnifiedMessage um : ums){
								Queueable obj = new Queueable(QueueType.UNIFIED_MESSAGE, um);
								toQueue.enqueue(6, obj);
							}
						} 

					} catch (ClassNotFoundException | NoSuchMethodException
							| SecurityException | InstantiationException
							| IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						logger.error("cannot queue unified messages from :" + message.getTopicName() );
					}
					// TODO: include priority parameter for the type of message. 

				}

				// queueable object type modbus
				if(queueable.getType() == QueueType.MODBUS_DEV_MESSAGE){

					@SuppressWarnings("unchecked")
					Map<String, Object> dictionary = (Map<String, Object>) queueable.getContent();
					Modbus2UnifiedMessage mod2Um = new	Modbus2UnifiedMessage(dictionary);
					List<UnifiedMessage> ums;

					try{
						ums = mod2Um.getUnifiedMessage();
						if (ums != null){
							//TODO: to define the priority for the message.
							for(UnifiedMessage um : ums){
								Queueable obj = new Queueable(QueueType.UNIFIED_MESSAGE, um);
								toQueue.enqueue(6, obj);
							}
						}
					}catch ( Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						logger.error("cannot queue unified messages from :" + dictionary.get("IPAddress") + dictionary.get("UID") );
					} 
				}

			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


}
