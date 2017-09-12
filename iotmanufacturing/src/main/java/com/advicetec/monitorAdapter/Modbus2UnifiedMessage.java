package com.advicetec.monitorAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.SampleMessage;
import com.advicetec.MessageProcessor.UnifiedMessage;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.MonitoringDevice;
import com.advicetec.eventprocessor.ModBusTcpEventType;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;
import com.advicetec.monitorAdapter.protocolconverter.Translator;
import com.advicetec.utils.ModBusUtils;

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


	
	@Override
	public List<UnifiedMessage> getUnifiedMessage() throws Exception {

		logger.debug("entering getUnifiedMessage" + "port:" + Integer.toString(this.port) + "type: " + this.type + " uid:" + this.uid + " offset: " + this.offSet + "count: " + this.count);
		
		ArrayList<UnifiedMessage> theList = new ArrayList<UnifiedMessage>();
		
		String portLabel = ModBusUtils.buildPortLabel(this.type, this.port, this.uid, this.offSet, this.count);

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
			
			if (type.equals(ModBusTcpEventType.READ_DISCRETE)) {
				Translator object = (Translator) Class.forName(classToLoad).newInstance();
				values = object.translate(dataMeasured);
				theList.add(new SampleMessage(device, device.getInputOutputPort(portLabel), measuringEntityId, values, transformation));
			} else if (type.equals(ModBusTcpEventType.READ_REGISTER)) {
				Translator object = (Translator) Class.forName(classToLoad).newInstance();
				values = object.translate(dataMeasured);				
				// Build the port label as: PREFIX + "-" + offset + "-" + count 
				theList.add(new SampleMessage(device, device.getInputOutputPort(portLabel), measuringEntityId, values, transformation));
			} else if (type.equals(ModBusTcpEventType.READ_HOLDING_REGISTER)) {
				Translator object = (Translator) Class.forName(classToLoad).newInstance();
				values = object.translate(dataMeasured);				
				// Build the port label as: PREFIX + "-" + offset + "-" + count 
				theList.add(new SampleMessage(device, device.getInputOutputPort(portLabel), measuringEntityId, values, transformation));
			} else {
				logger.error("The event type given:" + type.getName() + "has not an associated unified message");
			}
			
			logger.debug("exit getUnifiedMessage - Nbr Unified Messages:" + theList.size());
	
			return theList;
		}
	}
	

}
