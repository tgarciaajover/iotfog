package com.advicetec.configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;


public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> 
{    
    @Override
    public LocalDateTime deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
    	DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MMM-dd H:m:s.n");
        return LocalDateTime.parse(arg0.getText(),format);
    }

}
