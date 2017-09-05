package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.advicetec.core.MeasuringUnit;

/**
 * Deserialize a unit of measure object which is in JSON format. 
 * 
 * It takes the elements symbol and description and builds and unit of measure object. 
 * 
 * @author Andres Marentes
 *
 */
public class MeasuringUnitDeserializer extends JsonDeserializer<MeasuringUnit>{

	@Override
	public MeasuringUnit deserialize(JsonParser parser,
			DeserializationContext context) throws IOException,
			JsonProcessingException {
		JsonNode node = parser.getCodec().readTree(parser);
		
		return new MeasuringUnit(node.get("symbol").getTextValue(),node.get("description").getTextValue());
	}

}
