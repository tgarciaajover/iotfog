package com.advicetec.core.serialization;

import java.io.IOException;

import com.advicetec.measuredentitity.MeasuredAttributeValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class AttributeValueSerialization extends JsonSerializer<MeasuredAttributeValue> {

	public void serialize() {
		
	}

	@Override
	public void serialize(MeasuredAttributeValue measureAttValue, JsonGenerator jGen,
			SerializerProvider serializer) throws IOException,
			JsonProcessingException {
		jGen.writeString(measureAttValue.toString());
	}
}
