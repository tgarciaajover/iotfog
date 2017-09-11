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
import com.advicetec.eventprocessor.ModBusTcpEventType;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;
import com.advicetec.monitorAdapter.protocolconverter.Translator;

/**
 * This class implements the protocol conversion from MQTT message to 
 * <code>UnifiedMessage</code>.
 * This object is create from the <code>AdapterHandler</code> and implements
 * the <code>ProtocolConverter</code> interface, and its 
 * {@link #getUnifiedMessage()} method.
 * 
 * @author advicetec
 * @see UnifiedMessage
 * @see AdapterHandler 
 */
public class Mqtt2UnifiedMessage implements ProtocolConverter
{
	static Logger logger = LogManager.getLogger(Mqtt2UnifiedMessage.class.getName());
	
	private MqttPublish mqttMessage;

	/**
	 * Default constructor
	 */
	public Mqtt2UnifiedMessage(){

	}

	/**
	 * Constructor from <code>MqttPublish</code> object.
	 * @param mqttMsg MQTT message.
	 */
	public Mqtt2UnifiedMessage(MqttPublish mqttMsg){
		this.mqttMessage = mqttMsg;
	}

	/**
	 * Overrides <code>ProtocolConverter</code> interface.
	 * In this case, translate a MQTT message into the UnifiedMessage structure.
	 * The <ConfigurationManager> brings the Device, Transformation, and 
	 * Translator class to be loaded.
	 * The returning list contains <code>SampleMessage</code> objects with the 
	 * measured data.
	 * 
	 * @return a list of <code>UnifiedMessage</code> objects.
	 * @throws ClassNotFoundException if the loader cannot find the correct 
	 * class to transform the message.
	 * @throws IllegalAccessException if the default constructor of the class
	 * cannot be accessed. 
	 * @throws InstantiationException if the translator class has not a default
	 * constructor. 
	 * @see ModBusTcpEventType
	 * @see SampleMessage
	 * @see ConfigurationManager
	 */
	public List<UnifiedMessage> getUnifiedMessage() throws ClassNotFoundException, 
	NoSuchMethodException, SecurityException, InstantiationException, 
	IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		// facility/group/device/port
		String topic = mqttMessage.getTopicName();
		String[] fields = topic.split(SystemConstants.TOPIC_SEP);

		logger.debug("Complete topic:"+topic);
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
		// get the string for the right classname and package
		String packageStr = this.getClass().getPackage().getName() + ".protocolconverter";
		String classToLoad = packageStr + "." + className;

		List<InterpretedSignal> values;

		try {
			// loads the translator object from the specified classname.
			Translator object = (Translator) Class.forName(classToLoad).newInstance();

			values = object.translate(mqttMessage.getPayload());
			// TODO: call the measuredEntititiesFacade by mac address.
			Integer measuringEntityId = confManager.getMeasuredEntity(deviceID, portLabel);
			// creates the SampleMessage object.
			if (measuringEntityId != null){
				ArrayList<UnifiedMessage> theList = new ArrayList<UnifiedMessage>();
				theList.add(new SampleMessage(device, device.getInputOutputPort(portLabel), 
						measuringEntityId, values, transformation));
				return theList;
			} else {
				logger.error("No Measuring Entity Registered for the device:" + deviceID + 
						" port:" + portLabel);
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
