package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.advicetec.measuredentitity.MeasuringState;

/**
 * Convert from a json element into the measuring state enumeration. The following are the conversions executed:
 * 
 *  	O -> Operating
 *  	S -> Schedule Down
 *  	U -> Unschedule Down.
 * 
 * @author Andres Marentes
 *
 */
public class MeasuringStateDeserializer extends JsonDeserializer<MeasuringState>
{

	@Override
	public MeasuringState deserialize(JsonParser arg0, DeserializationContext arg1)
			throws IOException, JsonProcessingException {
		
		if (arg0.getText().compareTo("O") == 0){
			return MeasuringState.OPERATING;
		} else if (arg0.getText().compareTo("S") == 0){
			return MeasuringState.SCHEDULEDOWN;
		} else if (arg0.getText().compareTo("U") == 0){
			return MeasuringState.UNSCHEDULEDOWN;
		} else {
			return MeasuringState.UNDEFINED;
		}
		
	} 
	
}
