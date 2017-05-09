package com.advicetec.monitorAdapter.protocolconverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.AttributeType;
import com.advicetec.configuration.SystemConstants;
import com.advicetec.measuredentitity.MeasuredEntityContainer;

public class MqttSerialInput implements Translator
{

	static Logger logger = LogManager.getLogger(MqttSerialInput.class.getName());
	
	public MqttSerialInput(){
		super();
	}
	
	@Override
	public List<InterpretedSignal> translate(byte[] payload) 
	{

		List<InterpretedSignal> listReturn = new ArrayList<InterpretedSignal>();
		String content = new String(payload);
		
		// The mqtt message for a digital input payload has the following form:
		// topic | datetime | value
		
		try
		{
			String [] tokens = content.split(SystemConstants.MSG_SEP);
			
			String dateTimeStr = tokens[1]; 
			DateTimeFormatter format = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(SystemConstants.DATETIME_FORMAT).toFormatter();

			LocalDateTime datetimeObject = LocalDateTime.parse(dateTimeStr,format);
			InterpretedSignal currentdatetime = new InterpretedSignal(AttributeType.DATETIME, datetimeObject);
			
			String value = String.valueOf(tokens[2]);
			
			logger.debug("The following value arrives:" + value);
			
			InterpretedSignal valueSignal;
			valueSignal = new InterpretedSignal(AttributeType.STRING, value);

			logger.debug("The following value arrives:" + value);

			// listReturn.add(currentdatetime);
			listReturn.add(valueSignal);
			

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		return listReturn;
		
	}

}
