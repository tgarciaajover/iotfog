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

public class MqttDigital implements Translator
{
	static Logger logger = LogManager.getLogger(MqttDigital.class.getName());

	public MqttDigital()
	{
		super();
	}
	
	
	@Override
	public List<InterpretedSignal> translate(byte[] payload) 
	{

		List<InterpretedSignal> listReturn = new ArrayList<InterpretedSignal>();
		String content = new String(payload);
		
		// The mqtt message for a digital input payload has the following form:
		// topic | datetime | value
		
		logger.debug("content:" + content +"split token:"+ SystemConstants.MSG_SEP);
		String [] tokens = content.split(SystemConstants.MSG_SEP);
		
		for (int i = 0; i < tokens.length; i++) {
			logger.debug("token" + i + " value:" + tokens[i]);
		}
		
		String dateTimeStr = tokens[1]; 
		DateTimeFormatter format = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(SystemConstants.DATETIME_FORMAT).toFormatter();
		LocalDateTime datetimeObject = LocalDateTime.parse(dateTimeStr,format);
		InterpretedSignal currentdatetime = new InterpretedSignal(AttributeType.DATETIME, datetimeObject);
		
		Double value = Double.valueOf(tokens[2]);
		
		InterpretedSignal valueSignal;
		// TODO: replace constant values by constant defined in a property file.
		if ((value.floatValue() >= 0.0) && (value.floatValue() <= 0.5)){
			logger.debug("arrived value is interpreted as FALSE");
			boolean finalValue = false;
			valueSignal = new InterpretedSignal(AttributeType.BOOLEAN, new Boolean(finalValue));
		} else {
			logger.debug("arrived value is interpreted as TRUE");
			boolean finalValue = true;
			valueSignal = new InterpretedSignal(AttributeType.BOOLEAN, new Boolean(finalValue));
		}
		
		// listReturn.add(currentdatetime);
		listReturn.add(valueSignal);
		
		return listReturn;
	}

}
