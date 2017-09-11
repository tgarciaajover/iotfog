package com.advicetec.core.serialization;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;


/**
 * Deserialize a time json field into localtime. It uses the following pattern to deserialize: H:m:s.n 
 * 
 * @author Andres Marentes
 *
 */
public class LocalTimeDeserializer extends JsonDeserializer<LocalTime> 
{    
    /**
     * Deserialize a time json field into localtime. It uses the following pattern to deserialize: H:m:s.n
     */
    @Override
    public LocalTime deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
    	DateTimeFormatter format = DateTimeFormatter.ofPattern("H:m:s.n");
        return LocalTime.parse(arg0.getText(),format);
    }

}
