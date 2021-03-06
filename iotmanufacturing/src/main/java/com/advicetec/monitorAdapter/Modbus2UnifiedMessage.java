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
import com.advicetec.configuration.ModbusInputOutputPort;
import com.advicetec.configuration.ModbusMonitoringDevice;
import com.advicetec.configuration.MonitoringDevice;
import com.advicetec.eventprocessor.ModBusTcpEventType;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;
import com.advicetec.monitorAdapter.protocolconverter.Translator;

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
	private boolean isConcentrator; // the slave corresponds to a concentrator.
	private ModBusTcpEventType type; // type of modbus event
	private ModbusInputOutputPort inputOutputPort; // Modbus input output port when it is triggered by a single signal.
	private byte[] dataMeasured;   // measures


	public Modbus2UnifiedMessage(Map<String, Object> dictionary) {
		ipAddr = (String) dictionary.get("IPAddress");
		port = (Integer) dictionary.get("Port");
		uid = (Integer) dictionary.get("UID");
		offSet = (Integer) dictionary.get("Offset");
		count = (Integer) dictionary.get("Count");
		type = ModBusTcpEventType.from( ((int) dictionary.get("Type")) );
		isConcentrator = (boolean) dictionary.get("IsConcentrator");
		inputOutputPort = (ModbusInputOutputPort) dictionary.get("InputOutputPort");
		dataMeasured = (byte[]) dictionary.get("Read");
	}

	public List<UnifiedMessage> getUnifiedMessageSingleSignal() throws InstantiationException, 
	IllegalAccessException, ClassNotFoundException  {


		logger.info("entering getUnifiedMessageSingleSignal" + "ipAddr" + ipAddr + "port:"
				+ Integer.toString(port) + "type: " + this.type + 
				" uid:" + this.uid + " offset: " + this.offSet + 
				"count: " + this.count);

		ArrayList<UnifiedMessage> theList = new ArrayList<UnifiedMessage>();
						
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		MonitoringDevice device = confManager.getMonitoringDevice(ipAddr);
		
		if (device == null)
			logger.error("Device with ip address:" + ipAddr + "was not found" );
		
		String transformation = this.inputOutputPort.getTransformationText();
		String className = this.inputOutputPort.getSignalType().getType().getClassName();
	
		logger.debug("ClassName param:" + className);
		String packageStr = this.getClass().getPackage().getName() + ".protocolconverter";
		String classToLoad = packageStr + "." + className;
	
		List<InterpretedSignal> values;
		Integer measuringEntityId = this.inputOutputPort.getMeasuringEntity();
			
		// Depending on the Modbus Event, a different class is loaded.
		if (type.equals(ModBusTcpEventType.READ_DISCRETE)) {
			Translator object = (Translator) Class.forName(classToLoad).newInstance();
			values = object.translate(dataMeasured);
			theList.add(new SampleMessage(device, this.inputOutputPort, measuringEntityId, values, transformation));
				
		} else if (type.equals(ModBusTcpEventType.READ_REGISTER)) {
			Translator object = (Translator) Class.forName(classToLoad).newInstance();
			values = object.translate(dataMeasured);				
			// Build the port label as: PREFIX + "-" + offset + "-" + count 
			theList.add(new SampleMessage(device, this.inputOutputPort, measuringEntityId, values, transformation));
				
		} else if (type.equals(ModBusTcpEventType.READ_HOLDING_REGISTER)) {
			Translator object = (Translator) Class.forName(classToLoad).newInstance();
			values = object.translate(dataMeasured);				
			// Build the port label as: PREFIX + "-" + offset + "-" + count 
			theList.add(new SampleMessage(device, this.inputOutputPort, measuringEntityId, values, transformation));
				
		} else if (type == ModBusTcpEventType.ERROR_READ_DISCRETE) {	
			theList.add(new MeasuringErrorMessage(device, this.inputOutputPort, measuringEntityId));
				
		} else if (type == ModBusTcpEventType.ERROR_READ_REGISTER) {
				
			theList.add(new MeasuringErrorMessage(device, this.inputOutputPort, measuringEntityId));
				
		} else if (type == ModBusTcpEventType.ERROR_READ_HOLDING) {
				
			theList.add(new MeasuringErrorMessage(device, this.inputOutputPort, measuringEntityId));
				
		} else {
				
			logger.error("The event type given:" + type.getName() +
						"has not an associated unified message");
		}
			
		logger.debug("Exit getUnifiedMessage - Number of Unified Messages:" + theList.size());
	
		return theList;
	}

	public List<UnifiedMessage> getUnifiedMessageConcentrator() throws InstantiationException, 
	IllegalAccessException, ClassNotFoundException  {


		logger.debug("entering getUnifiedMessageConcentrator" + "port:"
				+ Integer.toString(port) + "type: " + this.type + 
				" uid:" + this.uid + " offset: " + this.offSet + 
				"count: " + this.count);
		

		ArrayList<UnifiedMessage> theList = new ArrayList<UnifiedMessage>();
		
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		ModbusMonitoringDevice device = (ModbusMonitoringDevice) confManager.getMonitoringDevice(ipAddr);
			
		List<ModbusInputOutputPort> ports = device.getInputOutputPorts();
		
		// This variable has the current position being read on the byte array.
		int startPosition = 1;

		logger.info("len data returned" +  String.valueOf(dataMeasured.length));
		for (byte b : dataMeasured) {
			logger.info("Orig Response:" + String.format("0x%02X", b));
		}

		
		for (int i = 0; i < ports.size(); i++) { 
			ModbusInputOutputPort port = ports.get(i);
			
			int nbrBytes = port.getNbr_read()*2;
			byte [] dataMeasuredForPort = new byte[nbrBytes + 1];
			
			dataMeasuredForPort[0] = (byte) nbrBytes;
			
			for (int k=0; k < nbrBytes; k++) {
				dataMeasuredForPort[k+1] = dataMeasured[k+startPosition];
			}
			
			for (byte b : dataMeasuredForPort) {
				logger.info("Response:" + String.format("0x%02X", b));
			}
			
			startPosition = startPosition + nbrBytes;
			
			String transformation = port.getTransformationText();
			String className = port.getSignalType().getType().getClassName();
	
			logger.debug("ClassName param:" + className);
			String packageStr = this.getClass().getPackage().getName() + ".protocolconverter";
			String classToLoad = packageStr + "." + className;
	
			List<InterpretedSignal> values;
			Integer measuringEntityId = port.getMeasuringEntity();
			
			// Depending on the Modbus Event, a different class is loaded.
			if (type.equals(ModBusTcpEventType.READ_DISCRETE)) {
				Translator object = (Translator) Class.forName(classToLoad).newInstance();
				values = object.translate(dataMeasuredForPort);
				theList.add(new SampleMessage(device, port,  
						measuringEntityId, values, transformation));
				
			} else if (type.equals(ModBusTcpEventType.READ_REGISTER)) {
				Translator object = (Translator) Class.forName(classToLoad).newInstance();
				values = object.translate(dataMeasuredForPort);				
				// Build the port label as: PREFIX + "-" + offset + "-" + count 
				theList.add(new SampleMessage(device, port, 
						measuringEntityId, values, transformation));
				
			} else if (type.equals(ModBusTcpEventType.READ_HOLDING_REGISTER)) {
				Translator object = (Translator) Class.forName(classToLoad).newInstance();
				values = object.translate(dataMeasuredForPort);				
				// Build the port label as: PREFIX + "-" + offset + "-" + count 
				theList.add(new SampleMessage(device, port, 
						measuringEntityId, values, transformation));
				
			} else if (type == ModBusTcpEventType.ERROR_READ_DISCRETE) {
				
				theList.add(new MeasuringErrorMessage(device, port, measuringEntityId));
				
			} else if (type == ModBusTcpEventType.ERROR_READ_REGISTER) {
				
				theList.add(new MeasuringErrorMessage(device, port, measuringEntityId));
				
			} else if (type == ModBusTcpEventType.ERROR_READ_HOLDING) {
				
				theList.add(new MeasuringErrorMessage(device, port, measuringEntityId));
				
			} else {
				
				logger.error("The event type given:" + type.getName() +
						"has not an associated unified message");
			}
			
			logger.debug("Exit getUnifiedMessage - Number of Unified Messages:" + theList.size());
	
		}

		return theList;
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

		if (!this.isConcentrator) { 
			return getUnifiedMessageSingleSignal();
		} else {
			return getUnifiedMessageConcentrator();
		}

		
	}
	

}
