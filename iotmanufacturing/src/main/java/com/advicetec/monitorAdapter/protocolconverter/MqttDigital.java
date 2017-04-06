package com.advicetec.monitorAdapter.protocolconverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;

import com.advicetec.FogClasses.AttributeType;
import com.advicetec.configuration.SystemConstants;

public class MqttDigital implements Translator
{

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
		
		String [] tokens = content.split(SystemConstants.MSG_SEP);
		
		String dateTimeStr = tokens[1]; 
		DateTimeFormatter format = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(SystemConstants.DATETIME_FORMAT).toFormatter();
		LocalDateTime datetimeObject = LocalDateTime.parse(dateTimeStr,format);
		InterpretedSignal currentdatetime = new InterpretedSignal(AttributeType.DATETIME, datetimeObject);
		
		Double value = Double.valueOf(tokens[2]);
		
		InterpretedSignal valueSignal;
		// TODO: replace constant values by constant defined in a property file.
		if ((value.floatValue() >= 0.0) && (value.floatValue() <= 0.5)){
			int finalValue = 0;
			valueSignal = new InterpretedSignal(AttributeType.INT, new Integer(finalValue));
		} else {
			int finalValue = 1;
			valueSignal = new InterpretedSignal(AttributeType.INT, new Integer(finalValue));
		}
		
		listReturn.add(currentdatetime);
		listReturn.add(valueSignal);
		
		return listReturn;
	}

}
