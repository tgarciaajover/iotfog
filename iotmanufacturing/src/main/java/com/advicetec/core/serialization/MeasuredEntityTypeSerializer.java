package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.advicetec.measuredentitity.MeasuredEntityType;

public class MeasuredEntityTypeSerializer extends JsonSerializer<MeasuredEntityType> 
{

	@Override
	public void serialize(MeasuredEntityType measuredEntityType, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		switch (measuredEntityType)
		{
			case MACHINE:
				jgen.writeString(new String("M"));
				break;
			case PLANT:
				jgen.writeString(new String("P"));
				break;
				
			case FACILITY:
				jgen.writeString(new String("F"));
				break;
				
			case COMPANY:
				jgen.writeString(new String("C"));
				break;
				
			case JOB:
				jgen.writeString(new String("J"));
				break;
		}
	}

}
