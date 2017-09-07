package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.MeasuringState;

/**
 * Converts from the measuring state enumeration into a json object with the following values:
 * 
 * 		Operating 		-> O
 * 		Schedule Down 	-> S
 * 		Unschedule Down -> U
 * 		Undefined		-> D
 * 
 * @author Andres Marentes
 *
 */
public class MeasuringStateSerializer extends JsonSerializer<MeasuringState> 
{

	@Override
	public void serialize(MeasuringState measuringState, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		switch (measuringState)
		{
			case OPERATING:
				jgen.writeString(new String("O"));
				break;
			case SCHEDULEDOWN:
				jgen.writeString(new String("S"));
				break;
				
			case UNSCHEDULEDOWN:
				jgen.writeString(new String("U"));
				break;
				
			case UNDEFINED:
				jgen.writeString(new String("D"));
				break;
				
		}
	}

}
