package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.advicetec.core.AttributeOrigin;

public class AttributeOriginDeserializer extends JsonDeserializer<AttributeOrigin> {

	@Override
	public AttributeOrigin deserialize(JsonParser parser,
			DeserializationContext arg1) throws IOException,
			JsonProcessingException {
		int code = parser.getCodec().readTree(parser).get("value").getIntValue();
		return AttributeOrigin.getByValue(code);
	}
}
