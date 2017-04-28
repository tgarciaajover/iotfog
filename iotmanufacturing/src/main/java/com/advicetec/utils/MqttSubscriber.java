package com.advicetec.utils;

import java.sql.Timestamp;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import com.advicetec.core.Configurable;
import com.advicetec.monitorAdapter.AdapterManager;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;

/**
 * A sample application that demonstrates how to use the Paho MQTT v3.1 Client blocking API.
 */
public class MqttSubscriber extends Configurable implements MqttCallback  {

	private String brokerUrl;
	private String userName;
	private String password;
	private String topicName;
	private String clientId;
	private int qos;
	AdapterManager adapterManager=null;
	
	public MqttSubscriber(){
		
		super("MqttSubscriber");
		
		brokerUrl = getProperty("BrokeUrl");
		userName = getProperty("UserName");
		password = getProperty("Password");
		topicName = getProperty("TopicName");
		clientId = getProperty("ClientId");
		qos = Integer.valueOf(getProperty("QoS"));
		adapterManager = AdapterManager.getInstance(); 
	}
	
	public void run(){
		
		
		MqttConnectOptions conOpt = new MqttConnectOptions();
		conOpt.setCleanSession(true);
		conOpt.setUserName(this.userName);	   
		conOpt.setPassword(this.password.toCharArray());

		// Construct an MQTT blocking mode client
		MqttClient client;
		try {
//			client = new MqttClient(this.brokerUrl, clientId, dataStore);
			client = new MqttClient(this.brokerUrl, clientId);
			// Set this wrapper as the callback handler
			client.setCallback(this);

			// Connect to the MQTT server
			client.connect(conOpt);

			// Create and configure a message
			byte[] payload = "mensaje".getBytes();
			MqttMessage message = new MqttMessage(payload);
			message.setQos(qos);

			client.publish(topicName, message); // Blocking publish

			// Subscribe to the requested topic
			// The QoS specified is the maximum level that messages will be sent to the client at. 
			// For instance if QoS 1 is specified, any messages originally published at QoS 2 will 
			// be downgraded to 1 when delivering to the client but messages published at 1 and 0 
			// will be received at the same level they were published at. 
//			log("Subscribing to topic \""+topicName+"\" qos "+qos);
			client.subscribe(topicName, qos);
			
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/****************************************************************/
	/* Methods to implement the MqttCallback interface              */
	/****************************************************************/

	/**
	 * @see MqttCallback#connectionLost(Throwable)
	 */
	public void connectionLost(Throwable cause) {
		// Called when the connection to the server has been lost.
		// An application may choose to implement reconnection
		// logic at this point. This sample simply exits.
//		log("Connection to " + brokerUrl + " lost!" + cause);
		System.exit(1);
	}

	/**
	 * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
	 */
	public void deliveryComplete(IMqttDeliveryToken token) {
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

	/**
	 * @see MqttCallback#messageArrived(String, MqttMessage)
	 */
	public void messageArrived(String topic, MqttMessage message) throws MqttException {
		// Called when a message arrives from the server that matches any
		// subscription made by the client		
		String time = new Timestamp(System.currentTimeMillis()).toString();
		System.out.println("Time:\t" +time +
				"  Topic:\t" + topic + 
				"  Message:\t" + new String(message.getPayload()) +
				"  QoS:\t" + message.getQos());
		MqttPublish mqttPublish = new MqttPublish(topic, message);
		System.out.println("message created");
		try {
			if (adapterManager == null)
				System.out.println("pailas es null");
			
			if (adapterManager.getQueue() == null)
				System.out.println("pailas queue es null");
			
			System.out.println("antes de ingresar a la cola"  );
			Queueable obj = new Queueable(QueueType.MQTT_DEV_MESSAGE, mqttPublish);
			adapterManager.getQueue().enqueue(6, obj);
			System.out.println("message enqueue" + mqttPublish.getTopicName() );
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		} 
	}

	/****************************************************************/
	/* End of MqttCallback methods                                  */
	/****************************************************************/

	static void printHelp() {
		System.out.println(
				"Syntax:\n\n" +
						"    Sample [-h] [-a publish|subscribe] [-t <topic>] [-m <message text>]\n" +
						"            [-s 0|1|2] -b <hostname|IP address>] [-p <brokerport>] [-i <clientID>]\n\n" +
						"    -h  Print this help text and quit\n" +
						"    -q  Quiet mode (default is false)\n" +
						"    -a  Perform the relevant action (default is publish)\n" +
						"    -t  Publish/subscribe to <topic> instead of the default\n" +
						"            (publish: \"Sample/Java/v3\", subscribe: \"Sample/#\")\n" +
						"    -m  Use <message text> instead of the default\n" +
						"            (\"Message from MQTTv3 Java client\")\n" +
						"    -s  Use this QoS instead of the default (2)\n" +
						"    -b  Use this name/IP address instead of the default (localhost)\n" +
						"    -p  Use this port instead of the default (1883)\n\n" +
						"    -i  Use this client ID instead of SampleJavaV3_<action>\n" +
						"    -c  Connect to the server with a clean session (default is false)\n" +
						"     \n\n Security Options \n" +
						"     -u Username \n" +
						"     -z Password \n" +
						"     \n\n SSL Options \n" +
						"    -v  SSL enabled; true - (default is false) " +
						"    -k  Use this JKS format key store to verify the client\n" +
						"    -w  Passpharse to verify certificates in the keys store\n" +
						"    -r  Use this JKS format keystore to verify the server\n" +
						" If javax.net.ssl properties have been set only the -v flag needs to be set\n" +
						"Delimit strings containing spaces with \"\"\n\n" +
						"Publishers transmit a single message then disconnect from the server.\n" +
						"Subscribers remain connected to the server and receive appropriate\n" +
						"messages until <enter> is pressed.\n\n"
				);
	}

}