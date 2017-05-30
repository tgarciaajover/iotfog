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

public class Modbus2UnifiedMessage implements ProtocolConverter {

	static Logger logger = LogManager.getLogger(Modbus2UnifiedMessage.class.getName());

	private String ipAddr; // Address of concentrator
	private Integer uid;   // id of device measure
	private ModBusTcpEventType type; // type of modbus event
	private String[] readDiscrete;   // measures


	public Modbus2UnifiedMessage(Map<String, Object> dictionary) {
		ipAddr = (String) dictionary.get("IPAddress");
		uid = (Integer) dictionary.get("UID");
		type = ModBusTcpEventType.from( ((int) dictionary.get("Type")) );
		readDiscrete = (String[]) dictionary.get("Read");
	}

	
	@Override
	public List<UnifiedMessage> getUnifiedMessage() throws Exception {

		String uidStr = Integer.toString(uid);

		ConfigurationManager confManager = ConfigurationManager.getInstance();
		MonitoringDevice device = confManager.getMonitoringDevice(ipAddr);
		String transformation = confManager.getTransformation(ipAddr, uidStr);
		String className = confManager.getClassName(ipAddr, uidStr);

		logger.debug("ClassName param:" + className);
		String packageStr = this.getClass().getPackage().getName() + ".protocolconverter";
		String classToLoad = packageStr + "." + className;

		List<InterpretedSignal> values;
		Integer measuringEntityId = confManager.getMeasuredEntity(ipAddr, uidStr);

		if(type.equals(ModBusTcpEventType.READ_DISCRETE)){
			Translator object = (Translator) Class.forName(classToLoad).newInstance();
			ArrayList<UnifiedMessage> theList = new ArrayList<UnifiedMessage>();
			for(String val: readDiscrete){
				values = object.translate(val.getBytes());
				theList.add(new SampleMessage(device, device.getInputOutputPort(uid), measuringEntityId, values, transformation));
			}
			return theList;
		}
		return null;
	}
	

}
