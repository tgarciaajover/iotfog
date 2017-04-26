package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.advicetec.core.Attribute;

public class AttributeSerializer extends JsonSerializer<Attribute> {

	@Override
	public void serialize(Attribute attr, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeStringField("name", attr.getName());
		jgen.writeObjectField("type", attr.getType());
		jgen.writeStringField("unit", attr.getUnit().toJson());
		jgen.writeEndObject();
	}

	
}
