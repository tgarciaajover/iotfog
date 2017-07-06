package com.advicetec.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.eventprocessor.ModBusTcpEventType;
import com.advicetec.measuredentitity.MeasuredEntityContainer;

public class ModBusUtils {

	static Logger logger = LogManager.getLogger(ModBusUtils.class.getName());
	
	private static final String PREFIX = "M";
	private static final String SEPARATOR = "-";
	private static final String REGISTER_READ = "RR";
	private static final String HOLDING_READ = "RH";
	private static final String DIGITAL_READ = "DR";
	private static final String REGISTER_WRITE = "RW";
	private static final String DIGITAL_WRITE = "DW";
	
	public static String buildPortLabel(ModBusTcpEventType type,  Integer unitId, Integer offSet, Integer count)
	{
		switch (type) {
			case READ_DISCRETE: 
				return PREFIX + SEPARATOR + DIGITAL_READ + SEPARATOR + Integer.toString(unitId) + SEPARATOR + Integer.toString(offSet) + SEPARATOR + Integer.toString(count);
			case READ_REGISTER:
				return PREFIX + SEPARATOR + REGISTER_READ + SEPARATOR + Integer.toString(unitId) + SEPARATOR + Integer.toString(offSet) + SEPARATOR + Integer.toString(count);
			case READ_HOLDING_REGISTER:
				return PREFIX + SEPARATOR + HOLDING_READ + SEPARATOR + Integer.toString(unitId) + SEPARATOR + Integer.toString(offSet) + SEPARATOR + Integer.toString(count);
			case WRITE_DISCRETE:
				return PREFIX + SEPARATOR + DIGITAL_WRITE + SEPARATOR + Integer.toString(unitId) + SEPARATOR + Integer.toString(offSet) + SEPARATOR + Integer.toString(count);
			case WRITE_REGISTER:
				return PREFIX + SEPARATOR + REGISTER_WRITE + SEPARATOR + Integer.toString(unitId) + SEPARATOR + Integer.toString(offSet) + SEPARATOR + Integer.toString(count);
			default:
				logger.error("The method buildPortLabel cannot create a port label with type:" + type.getName());
				return null;
		}
	}
	
	public static boolean isPortLabelValid(String portLabel){
		
		logger.info("Starting isPortLabelValid string:" + portLabel);
		String parts[] = portLabel.split(SEPARATOR);
		
		if (parts.length != 5)
			return false;
		
		if (parts[0].compareTo(PREFIX) != 0)
			return false;
		
		if ((parts[1].compareTo(REGISTER_READ) != 0) &&
			  (parts[1].compareTo(DIGITAL_READ) != 0) &&
			  (parts[1].compareTo(REGISTER_WRITE) != 0) && 
			  (parts[1].compareTo(DIGITAL_WRITE) != 0) &&
			  (parts[1].compareTo(HOLDING_READ) != 0))
			return false;
		
		try{
			
			Integer unitId = Integer.parseInt(parts[2]);
			Integer offset = Integer.parseInt(parts[3]);
			Integer count = Integer.parseInt(parts[4]);
			
		} catch (NumberFormatException nfe) {
			return false;
		}
		
		return true;
	}

	public static ModBusTcpEventType getModBusType(String portLabel){
		
		if (isPortLabelValid(portLabel) == true) {
			String parts[] = portLabel.split(SEPARATOR);
				
			if (parts[1].compareTo(REGISTER_READ) == 0){
				return ModBusTcpEventType.READ_REGISTER;
			} else if (parts[1].compareTo(DIGITAL_READ) == 0){
				return ModBusTcpEventType.READ_DISCRETE;
			} else if (parts[1].compareTo(REGISTER_WRITE) == 0){
				return ModBusTcpEventType.WRITE_REGISTER;
			} else if (parts[1].compareTo(DIGITAL_WRITE) == 0){
				return ModBusTcpEventType.WRITE_DISCRETE;
			} else if (parts[1].compareTo(HOLDING_READ) == 0){
				return ModBusTcpEventType.READ_HOLDING_REGISTER;
			} else {
				return ModBusTcpEventType.INVALID;
			}
				
		}
		
		return ModBusTcpEventType.INVALID;

	}
	
	public static Integer getUnitId(String portLabel){
		
		if (isPortLabelValid(portLabel) == true) {
			String parts[] = portLabel.split(SEPARATOR);
			try{
				
				return Integer.parseInt(parts[2]);
				
			} catch (NumberFormatException nfe) {
				return -1;
			}
			
		}
		
		return -1;
		
	}
	
	
	public static Integer getOffset(String portLabel){
		
		if (isPortLabelValid(portLabel) == true) {
			String parts[] = portLabel.split(SEPARATOR);
			try{
				
				return Integer.parseInt(parts[3]);
				
			} catch (NumberFormatException nfe) {
				return -1;
			}
			
		}
		
		return -1;
		
	}
	
	public static Integer getCount(String portLabel){

		if (isPortLabelValid(portLabel) == true) {
			String parts[] = portLabel.split(SEPARATOR);
			try{
				
				return Integer.parseInt(parts[4]);
				
			} catch (NumberFormatException nfe) {
				return -1;
			}
			
		}
		
		return -1;

	}
	
}
