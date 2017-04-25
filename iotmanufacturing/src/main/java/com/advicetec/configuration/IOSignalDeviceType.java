package com.advicetec.configuration;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class IOSignalDeviceType extends ConfigurationObject
{

	private Signal signal;
    
	@JsonProperty("i_o") 
	private String I_O;
    
	public IOSignalDeviceType(@JsonProperty("id") Integer id) {
		super(id);
	}
	
	public Signal getSignal() {
		return signal;
	}
	
	public void setSignal(Signal signal) {
		this.signal = signal;
	}
	
	public String getI_O() {
		return I_O;
	}
	
	public void setI_O(String i_O) {
		I_O = i_O;
	}
    
	public String toJson()
	{
		ObjectMapper mapper = new ObjectMapper();
			
		String jsonInString=null;
		try {
			
			jsonInString = mapper.writeValueAsString(this);
			
			
		} catch (JsonGenerationException e) {
			// TODO: log the error
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return jsonInString;
	}
    
	
}
