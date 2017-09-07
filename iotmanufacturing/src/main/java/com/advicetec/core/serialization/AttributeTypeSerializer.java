package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.advicetec.core.AttributeType;
import com.advicetec.core.MeasuringUnit;

import org.codehaus.jackson.JsonGenerator;

/**
 *  This class lets to serialize the attribute type into a json object
 * 
 * @author Andres Marentes
 *
 */
public class AttributeTypeSerializer extends JsonSerializer<AttributeType> {

	protected AttributeTypeSerializer(Class<MeasuringUnit> t) {
	}
	
	public AttributeTypeSerializer() {
		super();
	}

	/**
	 * Write a json with the following fields: value and name 
	 */
	@Override
	public void serialize(AttributeType value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			 JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeNumberField("value", value.getValue());
		jgen.writeStringField("name", value.getName());
		jgen.writeEndObject();
	}
}
