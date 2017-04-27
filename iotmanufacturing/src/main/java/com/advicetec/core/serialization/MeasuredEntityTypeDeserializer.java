package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.advicetec.measuredentitity.MeasuredEntityType;

public class MeasuredEntityTypeDeserializer extends JsonDeserializer<MeasuredEntityType>
{

	@Override
	public MeasuredEntityType deserialize(JsonParser arg0, DeserializationContext arg1)
			throws IOException, JsonProcessingException {
		
		if (arg0.getText().compareTo("M") == 0){
			return MeasuredEntityType.MACHINE;
		} else if (arg0.getText().compareTo("P") == 0){
			return MeasuredEntityType.PLANT;
		} else if (arg0.getText().compareTo("F") == 0){
			return MeasuredEntityType.FACILITY;
		} else if (arg0.getText().compareTo("C") == 0){
			return MeasuredEntityType.COMPANY;
		} else if (arg0.getText().compareTo("J") == 0){
			return MeasuredEntityType.JOB;
		}
		
		// By default return machine. 
		return MeasuredEntityType.MACHINE;
	} 
	
}
