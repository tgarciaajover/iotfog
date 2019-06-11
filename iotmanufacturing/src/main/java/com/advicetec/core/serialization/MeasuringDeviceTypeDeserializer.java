package com.advicetec.core.serialization;

import java.io.IOException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.advicetec.configuration.MonitoringDeviceType;


/**
 * This class lets to deserialize the measuring device type from json object
 * 
 * @author Andres Marentes
 *
 */
public class MeasuringDeviceTypeDeserializer extends JsonDeserializer<MonitoringDeviceType> {

	
	/**
	 * it takes the json element and returns the measuring device.
	 */
	@Override
	public MonitoringDeviceType deserialize(JsonParser parser,
			DeserializationContext arg1) throws IOException,
			JsonProcessingException {
		String name = parser.getCodec().readTree(parser).get("value").getTextValue();
		return MonitoringDeviceType.from_name(name);
	}

}
