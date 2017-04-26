package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.advicetec.core.AttributeOrigin;

public class AttributeOriginSerializer extends JsonSerializer<AttributeOrigin> {

	@Override
	public void serialize(AttributeOrigin origin, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeNumber(origin.getValue());
		jgen.writeString(origin.getName());
		jgen.writeEndObject();
	}

}
