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
 * Main functionality is to translate a boolean signal in MQTT protocol to a
 * collection of interpreted signals.
 * This class implement the <code>Translator</code> interface.
 * <p>
 * <i>value</i> is translate to FALSE if <i>MIN &lt; value &lt; MAX</i>, and 
 * translate to TRUE if <i>MAX &lt; value</i>.
 * @author advicetec
 * @see Translator
 */
public class MqttDigital implements Translator
{
	/**
	 * threshold constants: 
	 * MIN < value < MAX -> FALSE
	 * MAX < value -> TRUE
	 */
	private final static double MIN_THRESHOLD = 0.0;
	private final static double MAX_THRESHOLD = 5.0;
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
		// splits the string to get the topic and content
		String [] tokens = content.split(SystemConstants.MSG_SEP);
		
		for (int i = 0; i < tokens.length; i++) {
			logger.debug("token" + i + " value:" + tokens[i]);
		}
		
		// datetime stamp
		String dateTimeStr = tokens[1];
		DateTimeFormatter format = new DateTimeFormatterBuilder().
				parseCaseInsensitive()
				.appendPattern(SystemConstants.DATETIME_FORMAT).toFormatter();
		LocalDateTime datetimeObject = LocalDateTime.parse(dateTimeStr,format);
		@SuppressWarnings("unused")
		InterpretedSignal currentdatetime = new InterpretedSignal(AttributeType.DATETIME, datetimeObject);
		
		// value extraction
		Double value = Double.valueOf(tokens[2]);
		boolean finalValue = false;
		
		InterpretedSignal valueSignal;

		if ((value.floatValue() >= MIN_THRESHOLD) && (value.floatValue() <= MAX_THRESHOLD)){
			logger.debug("arrived value is interpreted as FALSE");
			finalValue = false;
		} else if(value.floatValue() > MAX_THRESHOLD) {
			logger.debug("arrived value is interpreted as TRUE");
			finalValue = true;
		}else{
			logger.warn("arrived value: "+ value.floatValue()+" has't been interpreted."
					+ "Default assignation to FALSE.");
			finalValue = false;
		}
		// assignates the translation
		valueSignal = new InterpretedSignal(AttributeType.BOOLEAN, new Boolean(finalValue));
		// listReturn.add(currentdatetime);
		listReturn.add(valueSignal);
		
		return listReturn;
	}

}
