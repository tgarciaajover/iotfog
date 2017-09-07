package com.advicetec.core.serialization;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;


/**
 * Deserialize a datetime json field into localdatetime. It uses the following pattern to deserialize: yyyy-MM-dd H:m:s.n 
 * 
 * @author Andres Marentes
 *
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> 
{    
    /**
     * Deserialize a datetime json field into localdatetime. It uses the following pattern to deserialize: yyyy-MM-dd H:m:s.n
     */
    @Override
    public LocalDateTime deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
    	DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s.n");
        return LocalDateTime.parse(arg0.getText(),format);
    }

}
