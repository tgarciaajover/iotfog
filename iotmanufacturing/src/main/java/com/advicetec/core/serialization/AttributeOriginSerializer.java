package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.advicetec.core.AttributeOrigin;

/**
 * This class lets serialize an attribute origin. It converts from the internal attribute origin code into json element
 * 
 * @author Andres Marentes
 *
 */
public class AttributeOriginSerializer extends JsonSerializer<AttributeOrigin> {

	/**
	 * Converts from the internal attribute origin code into json element
	 */
	@Override
	public void serialize(AttributeOrigin origin, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeNumberField("value", origin.getValue());
		jgen.writeStringField("name", origin.getName());
		jgen.writeEndObject();
	}

}
