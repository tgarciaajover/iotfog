package com.advicetec.configuration;

import java.io.IOException;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;

public class SignalUnit extends ConfigurationObject
{
	
	static Logger logger = LogManager.getLogger(SignalUnit.class.getName());
	
	/**
	 * description
	 */
	@JsonProperty("descr") 
	private String descr;
	
	/**
	 * registration date and time
	 */
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime create_date;
	
	/**
	 * Constructor for the class 
	 * 
	 * @param id  identifier of the signal unit
	 */
	@JsonCreator
	public SignalUnit(@JsonProperty("id") Integer id) {
		super(id);
	}
		
	/**
	 * Gets the description 
	 * 
	 * @return signal unit description.
	 */
	public String getDescr() {
		return descr;
	}
	
	/**
	 * Sets the description of the signal unit
	 * 
	 * @param descr description of the signal unit
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}
	
	/**
	 * Gets the registration date and time 
	 * 
	 * @return registration date and time
	 */
	public LocalDateTime getCreate_date() {
		return create_date;
	}
	
	/**
	 * Sets the registration date and time
	 * 
	 * @param create_date registration date and time
	 */
	public void setCreate_date(LocalDateTime create_date) {
		this.create_date = create_date;
	}
	
	/**
	 * Serialize the signal unit to json.
	 * 
	 * @return json representation of the signal unit.
	 */
	public String toJson()
	{
		ObjectMapper mapper = new ObjectMapper();
			
		String jsonInString=null;
		try {
			
			jsonInString = mapper.writeValueAsString(this);
			
			
		} catch (JsonGenerationException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return jsonInString;
	}	
}
