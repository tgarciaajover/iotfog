package com.advicetec.monitorAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.MeasuringErrorMessage;
import com.advicetec.MessageProcessor.SampleMessage;
import com.advicetec.MessageProcessor.UnifiedMessage;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.MonitoringDevice;
import com.advicetec.eventprocessor.ModBusTcpEventType;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;
import com.advicetec.monitorAdapter.protocolconverter.Translator;
import com.advicetec.utils.ModBusUtils;

/**
 * This class implements the protocol conversion from Modbus to 
 * <code>UnifiedMessage</code>.
 * This object is create from the <code>AdapterHandler</code> and implements
 * the <code>ProtocolConverter</code> interface, and its 
 * {@link #getUnifiedMessage()} method.
 * It also 
 * 
 * @author advicetec
 * @see UnifiedMessage
 * @see AdapterHandler 
 */
public class Modbus2UnifiedMessage implements ProtocolConverter {

	static Logger logger = LogManager.getLogger(Modbus2UnifiedMessage.class.getName());

	private String ipAddr; // Concentrator's ip address
	private Integer port;  // Concentrator's port
	private Integer uid;   // id of device measure
	private Integer offSet; // Number of the first register
	private Integer count;   // Number of elements read. 
	private ModBusTcpEventType type; // type of modbus event
	private byte[] dataMeasured;   // measures


	public Modbus2UnifiedMessage(Map<String, Object> dictionary) {
		ipAddr = (String) dictionary.get("IPAddress");
		port = (Integer) dictionary.get("Port");
		uid = (Integer) dictionary.get("UID");
		offSet = (Integer) dictionary.get("Offset");
		count = (Integer) dictionary.get("Count");
		type = ModBusTcpEventType.from( ((int) dictionary.get("Type")) );
		dataMeasured = (byte[]) dictionary.get("Read");
	}


	/**
	 * Overrides <code>ProtocolConverter</code> interface.
	 * In this case, translate a Modbus message into the UnifiedMessage structure.
	 * The type of Modbus event defines the class to be loaded.
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
	 */
	@Override
	public List<UnifiedMessage> getUnifiedMessage() throws InstantiationException, 
	IllegalAccessException, ClassNotFoundException  {


		logger.debug("entering getUnifiedMessage" + "port:"
				+ Integer.toString(port) + "type: " + this.type + 
				" uid:" + this.uid + " offset: " + this.offSet + 
				"count: " + this.count);

		
		ArrayList<UnifiedMessage> theList = new ArrayList<UnifiedMessage>();
		// get the portlabel from configuration environment.
		String portLabel = ModBusUtils.buildPortLabel(this.type, this.port, 
				this.uid, this.offSet, this.count);
		
		
		if (portLabel == null){
			return theList;
		} else {
			ConfigurationManager confManager = ConfigurationManager.getInstance();
			MonitoringDevice device = confManager.getMonitoringDevice(ipAddr);
			String transformation = confManager.getTransformation(ipAddr, portLabel);
			String className = confManager.getClassName(ipAddr, portLabel);
	
			logger.debug("ClassName param:" + className);
			String packageStr = this.getClass().getPackage().getName() + ".protocolconverter";
			String classToLoad = packageStr + "." + className;
	
			List<InterpretedSignal> values;
			Integer measuringEntityId = confManager.getMeasuredEntity(ipAddr, portLabel);
			
			// Depending on the Modbus Event, a different class is loaded.
			if (type.equals(ModBusTcpEventType.READ_DISCRETE)) {
				Translator object = (Translator) Class.forName(classToLoad).newInstance();
				values = object.translate(dataMeasured);
				theList.add(new SampleMessage(device, device.getInputOutputPort(portLabel), 
						measuringEntityId, values, transformation));
				
			} else if (type.equals(ModBusTcpEventType.READ_REGISTER)) {
				Translator object = (Translator) Class.forName(classToLoad).newInstance();
				values = object.translate(dataMeasured);				
				// Build the port label as: PREFIX + "-" + offset + "-" + count 
				theList.add(new SampleMessage(device, device.getInputOutputPort(portLabel), 
						measuringEntityId, values, transformation));
				
			} else if (type.equals(ModBusTcpEventType.READ_HOLDING_REGISTER)) {
				Translator object = (Translator) Class.forName(classToLoad).newInstance();
				values = object.translate(dataMeasured);				
				// Build the port label as: PREFIX + "-" + offset + "-" + count 
				theList.add(new SampleMessage(device, device.getInputOutputPort(portLabel), 
						measuringEntityId, values, transformation));
				
			} else if (type == ModBusTcpEventType.ERROR_READ_DISCRETE) {
				
				theList.add(new MeasuringErrorMessage(device, device.getInputOutputPort(portLabel), measuringEntityId));
				
			} else if (type == ModBusTcpEventType.ERROR_READ_REGISTER) {
				
				theList.add(new MeasuringErrorMessage(device, device.getInputOutputPort(portLabel), measuringEntityId));
				
			} else if (type == ModBusTcpEventType.ERROR_READ_HOLDING) {
				
				theList.add(new MeasuringErrorMessage(device, device.getInputOutputPort(portLabel), measuringEntityId));
				
			} else {
				
				logger.error("The event type given:" + type.getName() +
						"has not an associated unified message");
			}
			
			logger.debug("Exit getUnifiedMessage - Number of Unified Messages:" + theList.size());
	
			return theList;
		}
	}
	

}
