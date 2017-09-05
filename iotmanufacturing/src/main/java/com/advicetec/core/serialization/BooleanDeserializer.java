package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;


/**
 * Deserialize a JSON object which has two possible values Y - yes, N - Not. It yes the  it returns true, otherwise false
 * 
 * @author Andres Marentes
 *
 */
public class BooleanDeserializer extends JsonDeserializer<Boolean> 
{    
    /**
     * Y equals to true, false otherwise.
     */
    @Override
    public Boolean deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
    	if (arg0.getText().compareTo("Y") == 0){
    		return new Boolean(true);
    	} 
    	
        return new Boolean(false);
    }

}