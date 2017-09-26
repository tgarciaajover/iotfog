package com.advicetec.core.serialization;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

/**
 * Serialize a localtime into json field. It uses the following pattern to serialize: H:m:s.n 
 * 
 * @author andres
 *
 */
public class LocalTimeSerializer extends JsonSerializer<LocalTime> {

	    /**
	     *  Serialize a localtime into json field. It uses the following pattern to serialize: H:m:s.n 
	     */
	    @Override
	    public void serialize(LocalTime arg0, JsonGenerator arg1, SerializerProvider arg2) throws IOException, JsonProcessingException {
	    	DateTimeFormatter format = DateTimeFormatter.ofPattern("H:m:s.n");
	        arg1.writeString(arg0.format(format));
	    }
	
}
