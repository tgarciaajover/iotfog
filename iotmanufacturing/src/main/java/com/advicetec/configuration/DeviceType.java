package com.advicetec.configuration;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

public class DeviceType extends ConfigurationObject
{
	
	@JsonProperty("descr") 
	private String descr;
	
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
    private LocalDateTime create_date;

	
	@JsonProperty("io_signals")
	protected List<IOSignalDeviceType> signals;
    
	@JsonCreator
	public DeviceType(@JsonProperty("id") Integer id) {
		super(id);
		signals = new ArrayList<IOSignalDeviceType>();
	}
	
	public String getDescr() {
		return descr;
	}
	
	public void setDescr(String descr) {
		this.descr = descr;
	}
	
	public LocalDateTime getCreate_date() {
		return create_date;
	}
	
	public void setCreate_date(LocalDateTime create_date) {
		this.create_date = create_date;
	}  
    
	public void putIOSignal(IOSignalDeviceType signal)
	{
		this.signals.add(signal);
	}
	
	public IOSignalDeviceType getIOSignal(Integer signalId)
	{
		for (int i = 0; i < this.signals.size(); i++){
			if (this.signals.get(i).getId() == signalId){
				return this.signals.get(i);
			}
		}
		
		return null;
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
	
	@JsonIgnore
	public int getNumberSignals(){
		return this.signals.size();
	}
	
}
