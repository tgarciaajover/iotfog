package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.advicetec.core.MeasuringUnit;
import org.codehaus.jackson.JsonGenerator;

public class MeasuringUnitSerializer extends JsonSerializer<MeasuringUnit> {

	protected MeasuringUnitSerializer(Class<MeasuringUnit> t) {
	}

	@Override
	public void serialize(MeasuringUnit value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			 JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeStringField("symbol", value.getSymbol());
		jgen.writeStringField("description", value.getDescription());
		jgen.writeEndObject();
	}
}
