package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.advicetec.measuredentitity.MeasuredAttributeValue;

public class MeasuredAttributeValueDeserializer extends JsonDeserializer<MeasuredAttributeValue> {

	public MeasuredAttributeValueDeserializer() {
		super();
	}
	
	@Override
	public MeasuredAttributeValue deserialize(JsonParser parser,
			DeserializationContext context) throws IOException,
			JsonProcessingException {
		JsonNode node = parser.getCodec().readTree(parser);
		//TODO
		for (JsonNode jsonNode : node) {
		}
		return null;
	}

}
