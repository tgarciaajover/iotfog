package com.advicetec.core.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

public class BooleanSerializer extends JsonSerializer<Boolean> {

    @Override
    public void serialize(Boolean arg0, JsonGenerator arg1, SerializerProvider arg2) throws IOException, JsonProcessingException {
        if (arg0 == true){
        	arg1.writeString("Y");
        } else {
        	arg1.writeString("N");
        }
    }
}