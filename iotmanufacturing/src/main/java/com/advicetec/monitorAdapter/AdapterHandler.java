package com.advicetec.monitorAdapter;

import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;

import com.advicetec.MessageProcessor.UnifiedMessage;
import com.advicetec.monitorAdapter.protocolconverter.MqttSerialInput;
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
				Queueable queueable = (Queueable) fromQueue.pop();
				logger.debug("a queueable object was found");
				if (queueable.getType() == QueueType.MQTT_DEV_MESSAGE)
				{
					
					MqttPublish message = (MqttPublish) queueable.getContent(); 
					Mqtt2UnifiedMessage mq2Um = new Mqtt2UnifiedMessage(message);
					UnifiedMessage um;
					try 
					{
						um = mq2Um.getUnifiedMessage();
						
						if (um != null){
							//TODO: to define the priority for the message.
							Queueable obj = new Queueable(QueueType.UNIFIED_MESSAGE, um);
							toQueue.enqueue(6, obj);
						} 
						
					} catch (ClassNotFoundException | NoSuchMethodException
							| SecurityException | InstantiationException
							| IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// TODO: include priority parameter for the type of message. 
					
				}		
				
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	
}
