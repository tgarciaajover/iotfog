package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.advicetec.core.AttributeType;

/**
 * This class lets to deserialize the attribute type from json object
 * 
 * @author Andres Marentes
 *
 */
public class AttributeTypeDeserializer extends JsonDeserializer<AttributeType> {

	
	/**
	 * it takes the json element and returns the internal attribute type code.
	 */
	@Override
	public AttributeType deserialize(JsonParser parser,
			DeserializationContext arg1) throws IOException,
			JsonProcessingException {
		int code = parser.getCodec().readTree(parser).get("value").getIntValue();
		return AttributeType.getByValue(code);
	}

}
