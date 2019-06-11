package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.advicetec.configuration.ModbusAccess;

/**
 * This class lets to deserialize the attribute type from json object
 * 
 * @author Andres Marentes
 *
 */
public class ModbusAccessDeserializer extends JsonDeserializer<ModbusAccess> {

	
	/**
	 * it takes the json element and returns the internal attribute type code.
	 */
	@Override
	public ModbusAccess deserialize(JsonParser parser,
			DeserializationContext arg1) throws IOException,
			JsonProcessingException {	
		System.out.print("access text:" + parser.getText());
		String value = parser.getText();
		if (value.isEmpty()){
			return ModbusAccess.INVALID;
		} else {
			return ModbusAccess.from(Integer.parseInt(value));
		}
	}

}
