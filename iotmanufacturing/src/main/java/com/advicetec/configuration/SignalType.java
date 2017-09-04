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

public class SignalType extends ConfigurationObject
{

	static Logger logger = LogManager.getLogger(SignalType.class.getName());
	
	/**
	 *  Name assigned to te signal type 
	 */
	@JsonProperty("name") private String name;
	
	/**
	 * Name of the class that will be used to interpret the signal. 
	 * 
	 * Interpreted classes should be created on package com.advicetec.monitoradapter.protocolconverter.
	 */
	@JsonProperty("class_name") private String className;

	/**
	 * date and time when the signal type was registered in the system.
	 */
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime create_date;
	
	/**
	 * Protocol being used by the signal type (for now modbus and mqtt)
	 */
	@JsonProperty("protocol")
	private String protocol;
	
	/**
	 * Gets the protocol being used by the signal type.
	 * 
	 * @return protocol used.
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Sets the protocol being used by the signal type
	 * 
	 * @param protocol  protocol to set.
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * Constructor for the signal type 
	 * 
	 * @param id signal's type identifier
	 */
	@JsonCreator
	public SignalType(@JsonProperty("id") Integer id) {
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
	 * Gets the class name used to interpret the signal.
	 * 
	 * @return class name.
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * Sets the class name used to interpret the signal.
	 * 
	 * @param class_name
	 */
	public void setClassName(String class_name) {
		this.className = class_name;
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
