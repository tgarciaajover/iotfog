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

public class AggregateMethod extends ConfigurationObject
{

	static Logger logger = LogManager.getLogger(AggregateMethod.class.getName());
	
	@JsonProperty("name")	private String name;
	@JsonProperty("descr")	private String descr;
	@JsonProperty("method")	private String method;
	@JsonProperty("attribute")	private String attribute;
	@JsonProperty("period_id")	private String period_id;
	@JsonProperty("name_period")	private String name_period;
	@JsonProperty("type")	private String type;
	@JsonProperty("type_child")	private String type_child;
	@JsonProperty("scale")	private int scale;
	
	/**
	 * date and time when the signal type was registered in the system.
	 */
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime create_date;
	

	/**
	 * Constructor for the signal type 
	 * 
	 * @param id signal's type identifier
	 */
	@JsonCreator
	public AggregateMethod(@JsonProperty("id") Integer id) {
		super(id);
	}

	/**
	 * Set the identifier of the signal type.
	 */
	public void setId(Integer id) {
		this.id = id;
	}	
	
	/**
	 * Gets the name of the signal type
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of the signal type.
	 * 
	 * @param name name to be assigned.
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @param create_date  registration date and time
	 */
	public void setCreate_date(LocalDateTime create_date) {
		this.create_date = create_date;
	}


	
	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getPeriod_id() {
		return period_id;
	}

	public void setPeriod_id(String period_id) {
		this.period_id = period_id;
	}

	public String getName_period() {
		return name_period;
	}

	public void setName_period(String name_period) {
		this.name_period = name_period;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType_child() {
		return type_child;
	}

	public void setType_child(String type_child) {
		this.type_child = type_child;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	/**
	 * Serialize the signal type to a JSON object.
	 * 
	 * @return json object representing the signal type.
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
