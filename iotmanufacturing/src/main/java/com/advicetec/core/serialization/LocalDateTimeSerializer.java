package com.advicetec.core.serialization;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

	    @Override
	    public void serialize(LocalDateTime arg0, JsonGenerator arg1, SerializerProvider arg2) throws IOException, JsonProcessingException {
	    	DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MMM-dd H:m:s.n");
	        arg1.writeString(arg0.format(format));
	    }
	
}
