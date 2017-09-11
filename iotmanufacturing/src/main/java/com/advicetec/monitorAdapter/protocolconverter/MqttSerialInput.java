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

/**
 * Main functionality is to translate a serial (String) signal in MQTT protocol
 *  to a collection of interpreted signals.
 * This class implement the <code>Translator</code> interface.
 * 
 * @author advicetec
 * @see Translator
 */
public class MqttSerialInput implements Translator
{

	static Logger logger = LogManager.getLogger(MqttSerialInput.class.getName());

	public MqttSerialInput(){
		super();
	}

	@SuppressWarnings("unused")
	@Override
	public List<InterpretedSignal> translate(byte[] payload) 
	{

		List<InterpretedSignal> listReturn = new ArrayList<InterpretedSignal>();
		String content = new String(payload);

		// The mqtt message for a digital input payload has the following form:
		// topic | datetime | value

		String [] tokens = content.split(SystemConstants.MSG_SEP);
		// get datetime
		String dateTimeStr = tokens[1]; 
		DateTimeFormatter format = new DateTimeFormatterBuilder().
				parseCaseInsensitive().
				appendPattern(SystemConstants.DATETIME_FORMAT).toFormatter();
		LocalDateTime datetimeObject = LocalDateTime.parse(dateTimeStr,format);
		InterpretedSignal currentdatetime = 
				new InterpretedSignal(AttributeType.DATETIME, datetimeObject);

		// get value
		String value = String.valueOf(tokens[2]);
		logger.debug("The following value arrives:" + value);
		InterpretedSignal valueSignal = 
				new InterpretedSignal(AttributeType.STRING, value);

		// listReturn.add(currentdatetime);
		listReturn.add(valueSignal);

		return listReturn;

	}

}
