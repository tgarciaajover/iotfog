package com.advicetec.monitorAdapter;

import java.time.LocalDateTime;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

import com.advicetec.MessageProcessor.SampleMessage;
import com.advicetec.MessageProcessor.UnifiedMessage;
import com.advicetec.configuration.MonitoringDevice;
import com.advicetec.configuration.SystemConstants;

public class Mqtt2UnifiedMessage implements ProtocolConverter{

	private MqttPublish mqttMessage;
	private UnifiedMessage unifiedMessage;
	
	public Mqtt2UnifiedMessage(){
		
	}
	
	public Mqtt2UnifiedMessage(MqttPublish mqttMsg){
		this.mqttMessage = mqttMsg;
	}
	
	
	public UnifiedMessage getUnifiedMessage() throws Exception {
		// facility/group/device/port
		String topic = mqttMessage.getTopicName();
		String[] fields = topic.split(SystemConstants.MSG_SEP);
		MonitoringDevice device = null; //TODO
		try {
			return new SampleMessage(SampleType.SAMPLE,mqttMessage.getPayload());
		} catch (MqttException e) {
			e.printStackTrace();
			throw new Exception("Error creating the unified message.");
		}
	}

}
