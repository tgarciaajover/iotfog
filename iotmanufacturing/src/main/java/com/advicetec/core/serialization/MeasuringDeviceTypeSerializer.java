package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.advicetec.configuration.MonitoringDeviceType;
import com.advicetec.configuration.ModbusObjectType;

/**
 *  This class lets to serialize the Measuring Device Type into a json object
 * 
 * @author Andres Marentes
 *
 */
public class MeasuringDeviceTypeSerializer extends JsonSerializer<MonitoringDeviceType> {
	
	public MeasuringDeviceTypeSerializer() {
		super();
	}

	/**
	 * Write a json with the following fields: value and name 
	 */
	@Override
	public void serialize(MonitoringDeviceType value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			 JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeNumberField("value", value.getValue());
		jgen.writeStringField("name", value.getName());
		jgen.writeEndObject();
	}
}
