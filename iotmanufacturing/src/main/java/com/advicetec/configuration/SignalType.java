package com.advicetec.configuration;

import java.io.IOException;
import java.time.LocalDateTime;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;

public class SignalType extends ConfigurationObject
{

	@JsonProperty("name") private String name;
	@JsonProperty("class_name") private String className;

	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime create_date;
	
	@JsonProperty("protocol")
	private String protocol;
	
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@JsonCreator
	public SignalType(@JsonProperty("id") Integer id) {
		super(id);
	}

	public void setId(Integer id) {
		this.id = id;
	}	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String class_name) {
		this.className = class_name;
	}

	public LocalDateTime getCreate_date() {
		return create_date;
	}
	
	public void setCreate_date(LocalDateTime create_date) {
		this.create_date = create_date;
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
