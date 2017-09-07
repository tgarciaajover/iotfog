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

public class Signal extends ConfigurationObject
{

	static Logger logger = LogManager.getLogger(Signal.class.getName());
	
	/**
	 * Unit of the signal
	 */
	private SignalUnit unit;
	
	/**
	 * Type of the signal
	 */
	private SignalType type;
	
	/**
	 * description
	 */
	@JsonProperty("descr") 
	private String descr;
	
	/**
	 * date and time registration
	 */
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	private LocalDateTime create_date;
	
	/**
	 * Constructor for the signal
	 * 
	 * @param id identifier assigned by the database.
	 */
	@JsonCreator
	public Signal(@JsonProperty("id") Integer id) {
		super(id);
	}
	
	/**
	 * Gets the signal unit
	 * 
	 * @return signal unit
	 */
	public SignalUnit getUnit() {
		return unit;
	}
	
	/**
	 * Sets the signal unit
	 * 
	 * @param unit unit to be assigned
	 */
	public void setUnit(SignalUnit unit) {
		this.unit = unit;
	}
	
	/**
	 * Gets the signal type
	 * @return signal type or null
	 */
	public SignalType getType() {
		return type;
	}
	
	/**
	 * @param type
	 */
	public void setType(SignalType type) {
		this.type = type;
	}
	
	/**
	 * @return
	 */
	public String getDescr() {
		return descr;
	}
	
	/**
	 * @param descr
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}
	
	/**
	 * @return
	 */
	public LocalDateTime getCreate_date() {
		return create_date;
	}
	
	/**
	 * @param create_date
	 */
	public void setCreate_date(LocalDateTime create_date) {
		this.create_date = create_date;
	}

	/**
	 * Serialize the object to JSON
	 * 
	 * @return Json Object representing the signal.
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
