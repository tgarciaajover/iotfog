package com.advicetec.core.serialization;

import java.io.IOException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.advicetec.core.Attribute;

public class AttributeDeserializer extends JsonDeserializer<Attribute>{

	@Override
	public Attribute deserialize(JsonParser arg0, DeserializationContext arg1)
			throws IOException, JsonProcessingException {
		// TODO Auto-generated method stub
		return null;
	} 

}
