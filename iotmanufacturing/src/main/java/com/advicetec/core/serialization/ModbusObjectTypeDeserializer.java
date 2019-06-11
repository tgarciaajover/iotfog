package com.advicetec.core.serialization;

import java.io.IOException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.advicetec.configuration.ModbusObjectType;


/**
 * This class lets to deserialize the attribute type from json object
 * 
 * @author Andres Marentes
 *
 */
public class ModbusObjectTypeDeserializer extends JsonDeserializer<ModbusObjectType> {

	
	/**
	 * it takes the json element and returns the internal attribute type code.
	 */
	@Override
	public ModbusObjectType deserialize(JsonParser parser,
			DeserializationContext arg1) throws IOException,
			JsonProcessingException {
		
		String value = parser.getText();
		if (value.isEmpty() || (value == null)) {
			return ModbusObjectType.INVALID;
		} else {
			return ModbusObjectType.from(Integer.parseInt(value));
		}
	}

}
