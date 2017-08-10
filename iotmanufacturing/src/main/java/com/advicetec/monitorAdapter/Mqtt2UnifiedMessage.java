package com.advicetec.monitorAdapter;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;

import com.advicetec.MessageProcessor.SampleMessage;
import com.advicetec.MessageProcessor.UnifiedMessage;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.MonitoringDevice;
import com.advicetec.configuration.SystemConstants;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;
import com.advicetec.monitorAdapter.protocolconverter.Translator;

public class Mqtt2UnifiedMessage implements ProtocolConverter
{

	static Logger logger = LogManager.getLogger(Mqtt2UnifiedMessage.class.getName());
	private MqttPublish mqttMessage;
	
	public Mqtt2UnifiedMessage(){
		
	}
	
	public Mqtt2UnifiedMessage(MqttPublish mqttMsg){
		this.mqttMessage = mqttMsg;
	}
	
	
	public List<UnifiedMessage> getUnifiedMessage() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		// facility/group/device/port
		String topic = mqttMessage.getTopicName();
		String[] fields = topic.split(SystemConstants.TOPIC_SEP);
		
		for (int i = 0; i < fields.length; i++) {
			logger.debug("field: "+ i + "value:" + fields[i]);
		}
				
		String deviceID = fields[1];  // Device Id - example Mac Address: 10:10:10:10:10:10 
		String portLabel = fields[2]; // PortLabel - Example P01 

		ConfigurationManager confManager = ConfigurationManager.getInstance();
		
		MonitoringDevice device = confManager.getMonitoringDevice(deviceID);
		String transformation = confManager.getTransformation(deviceID, portLabel);
		String className = confManager.getClassName(deviceID, portLabel);
		
		logger.debug("className param:" + className);
		String packageStr = this.getClass().getPackage().getName() + ".protocolconverter";
		String classToLoad = packageStr + "." + className;

		List<InterpretedSignal> values;

		try {
			
			Translator object = (Translator) Class.forName(classToLoad).newInstance();
		
			values = object.translate(mqttMessage.getPayload());
			// TODO: call the measuredEntititiesFacade by mac address.
			
			 Integer measuringEntityId = confManager.getMeasuredEntity(deviceID, portLabel);
			 if (measuringEntityId != null){
				 ArrayList<UnifiedMessage> theList = new ArrayList<UnifiedMessage>();
				 theList.add(new SampleMessage(device, device.getInputOutputPort(portLabel), measuringEntityId, values, transformation));
				 return theList;
			 } else {
				 logger.error("No Measuring Entity Registered for the device:" + deviceID + " port:" + portLabel);
				 return null;
			 }

		} catch (MqttException e) {
			logger.error("Error in mqtt message" + e.getMessage());
			e.printStackTrace();
		}  catch (ClassNotFoundException e){
			logger.error("Error Class not found" + e.getMessage());
		}

		return null;
	}

}
