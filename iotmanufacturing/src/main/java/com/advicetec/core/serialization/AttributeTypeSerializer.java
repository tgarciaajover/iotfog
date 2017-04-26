package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.advicetec.core.AttributeType;
import com.advicetec.core.MeasuringUnit;

import org.codehaus.jackson.JsonGenerator;

public class AttributeTypeSerializer extends JsonSerializer<AttributeType> {

	protected AttributeTypeSerializer(Class<MeasuringUnit> t) {
	}
	
	public AttributeTypeSerializer() {
		super();
	}

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
