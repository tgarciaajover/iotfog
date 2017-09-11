package com.advicetec.emulators;

import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;

import com.advicetec.monitorAdapter.AdapterManager;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;

/**
 * This class emulates the Controller behavior by pushing MQTT messages
 * into the MQTT queue.
 * This class subscribes to the MQTT broker to receive messages.
 * 
 * @author advicetec
 */
public class FakeController implements MqttCallback{
	
	static Logger logger = LogManager.getLogger(FakeController.class.getName());
	
	private AdapterManager adapterManager;
	
	private String brokerUrl;
	private String userName;
	private String password;
	private String topicName;
	private String clientId;
	private int qos;
	
	public FakeController(AdapterManager adapter){
		adapterManager = adapter;
		brokerUrl="";
		userName="";
		password="";
		topicName="";
		clientId="";
		qos=0;
	}
	
	public void setAttributes(String brokerUrl, String userName, String password,
			String topicName, String clientId, int qos) {
		this.brokerUrl = brokerUrl;
		this.userName = userName;
		this.password = password;
		this.topicName = topicName;
		this.clientId = clientId;
		this.qos = qos;
	}
	
	public void run(){
		MqttConnectOptions conOpt = new MqttConnectOptions();
		conOpt.setCleanSession(true);
		conOpt.setUserName(this.userName);	   
		conOpt.setPassword(this.password.toCharArray());
		
		MqttClient client;
		try {
//			client = new MqttClient(this.brokerUrl, clientId, dataStore);
			client = new MqttClient(this.brokerUrl, clientId);
			// Set this wrapper as the callback handler
			client.setCallback(this);

			// Connect to the MQTT server
			client.connect(conOpt);
			client.subscribe(topicName, qos);
			
		} catch (MqttException e) {

			e.printStackTrace();
		}
	}

	public void connectionLost(Throwable arg0) {
		// Called when the connection to the server has been lost.
				// An application may choose to implement reconnection
				// logic at this point. This sample simply exits.
//				log("Connection to " + brokerUrl + " lost!" + cause);
				System.exit(1);
	}

	
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// Called when a message has been delivered to the
		// server. The token passed in here is the same one
		// that was passed to or returned from the original call to publish.
		// This allows applications to perform asynchronous 
		// delivery without blocking until delivery completes.
		//
		// This sample demonstrates asynchronous deliver and 
		// uses the token.waitForCompletion() call in the main thread which
		// blocks until the delivery has completed. 
		// Additionally the deliveryComplete method will be called if 
		// the callback is set on the client
		// 
		// If the connection to the server breaks before delivery has completed
		// delivery of a message will complete after the client has re-connected.
		// The getPendingTokens method will provide tokens for any messages
		// that are still to be delivered.
		
	}

	
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		// TODO Auto-generated method stub
		String time = new Timestamp(System.currentTimeMillis()).toString();
		logger.debug("Time:\t" +time +
				"  Topic:\t" + topic + 
				"  Message:\t" + new String(message.getPayload()) +
				"  QoS:\t" + message.getQos());
		MqttPublish publish = new MqttPublish(topic, message);
 
		publish.getTopicName();
		adapterManager.getQueue().enqueue(7, new Queueable(QueueType.MQTT_DEV_MESSAGE,publish));
	}
 
}
