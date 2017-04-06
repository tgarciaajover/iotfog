package com.advicetec.monitorAdapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

import com.advicetec.core.MeasuredEntity;
import com.advicetec.core.MeasuredEntityFacade;
import com.advicetec.MessageProcessor.SampleMessage;
import com.advicetec.MessageProcessor.UnifiedMessageType;
import com.advicetec.MessageProcessor.UnifiedMessage;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.MonitoringDevice;
import com.advicetec.configuration.SystemConstants;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;
import com.advicetec.monitorAdapter.protocolconverter.Translator;

public class Mqtt2UnifiedMessage implements ProtocolConverter
{

	private MqttPublish mqttMessage;
	private UnifiedMessage unifiedMessage;
	
	public Mqtt2UnifiedMessage(){
		
	}
	
	public Mqtt2UnifiedMessage(MqttPublish mqttMsg){
		this.mqttMessage = mqttMsg;
	}
	
	
	public UnifiedMessage getUnifiedMessage() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		// facility/group/device/port
		String topic = mqttMessage.getTopicName();
		String[] fields = topic.split(SystemConstants.MSG_SEP);
		String deviceID = fields[2];
		String portLabel = fields[3];

		ConfigurationManager confManager = ConfigurationManager.getInstance();
		
		MonitoringDevice device = confManager.getMonitoringDevice(deviceID);
		String transformation = confManager.getTransformation(deviceID, portLabel);
		String className = confManager.getClassName(deviceID, portLabel);
		
		String packageStr = this.getClass().getPackage().getName();
		String classToLoad = packageStr + "." + className;
		
		Class<?> c = Class.forName(classToLoad);
		Constructor<?> cons = c.getConstructor(String.class);
		Translator object = (Translator) cons.newInstance();
		
		List<InterpretedSignal> values;
		try {
			values = object.translate(mqttMessage.getPayload());
			// TODO: call the measuredEntititiesFacade by mac address.
			
			 String measuringEntityId = confManager.getMeasuredEntity(deviceID, portLabel);
			 if (measuringEntityId != null){
				 return new SampleMessage(device, device.getInputOutputPort(portLabel), measuringEntityId, values, transformation);
			 } else {
				 // TODO: register the error in the log file. 
				 System.out.println("No Measuring Entity Registered for the device:" + deviceID + " port:" + portLabel);
				 return null;
			 }
				 
			

		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

}
