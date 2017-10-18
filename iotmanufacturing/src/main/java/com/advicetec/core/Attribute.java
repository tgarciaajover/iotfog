package com.advicetec.core;

import java.io.IOException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.core.serialization.AttributeOriginSerializer;
import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.configuration.SignalType;
import com.advicetec.core.serialization.AttributeOriginDeserializer;
import com.advicetec.core.serialization.AttributeTypeDeserializer;
import com.advicetec.core.serialization.AttributeTypeSerializer;
import com.advicetec.core.serialization.MeasuringUnitSerializer;
import com.advicetec.core.serialization.MeasuringUnitDeserializer;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * Class to describes the attribute, Attributes can be assimilated to data types with additional parameters.
 *  
 * @author Andres Marentes
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Attribute 
{
	
	static Logger logger = LogManager.getLogger(Attribute.class.getName());
	
	/**
	 * Name assigned 
	 */
	private String name;
	
	/**
	 * Type of attribute. 
	 */
	@JsonSerialize(using = AttributeTypeSerializer.class)
	@JsonDeserialize(using = AttributeTypeDeserializer.class)
	private AttributeType type;
	
	/**
	 * Unit of measure, optional attribute.
	 */
	@JsonSerialize(using = MeasuringUnitSerializer.class)
	@JsonDeserialize(using = MeasuringUnitDeserializer.class)
	private MeasuringUnit unit;
	
	/**
	 * specifies if the attribute is trend or not. 
	 * Trend attributes identifies those that can be showed on the web application.
	 */
	private boolean trend;

	/**
	 * Identifies if the attribute was created by the transformation or behavior language.
	 */
	@JsonSerialize(using = AttributeOriginSerializer.class)
	@JsonDeserialize(using = AttributeOriginDeserializer.class)
	private AttributeOrigin origin;

	/**
	 * Main constructor for the class.
	 * 
	 * @param name  	name of the attribute 
	 * @param type  	type of attribute
	 * @param unit  	unit of measure assigned to the attribute
	 * @param trend 	trend indicator 
	 * @param origin	origin indicator.
	 */
	@JsonCreator
	public Attribute(@JsonProperty("name") String name,@JsonProperty("type") AttributeType type, 
			@JsonProperty("unit")MeasuringUnit unit, @JsonProperty("trend")boolean trend, 
			@JsonProperty("origin")AttributeOrigin origin) {
		super();
		this.name = name;
		this.type = type;
		this.unit = unit;
		this.trend = trend;
		this.origin = origin;
	}

	/**
	 * Alternative constructor for the class.
	 * 
	 * @param name  	name of the attribute
	 * @param type		type of attribute
	 * @param unit		unit of measure assigned to the attribute
	 */
	public Attribute(String name, AttributeType type,  MeasuringUnit unit) 
	{
		this.name = name;
		this.type = type;
		this.unit = unit;
		this.trend = false;
		this.origin = AttributeOrigin.BEHAVIOR;
	}

	/**
	 * Sets the trend indicator
	 * 
	 * @param newTrend  trend indicator
	 */
	public void setTrend(boolean newTrend){
		trend = newTrend;
	}

	/**
	 * Alternative constructor for the class.
	 * 
	 * @param name		name of the attribute
	 * @param type		type of attribute
	 */
	public Attribute(String name, AttributeType type){
		this(name,type,null);
	}

	/**
	 * Gets the name of the attribute.
	 * 
	 * @return
	 */
	@XmlAttribute
	public String getName() {
		return name;
	}

	/**
	 * Gets the unit of measure assigned to the attribute.
	 * 
	 * @return
	 */
	public MeasuringUnit getUnit() {
		return unit;
	}

	/**
	 * Gets the type of attribute assigned
	 * 
	 * @return
	 */
	public AttributeType getType()
	{
		return this.type;
	}

	/**
	 * Gets the trend indicator
	 * 
	 * @return
	 */
	@XmlAttribute
	public boolean getTrend(){
		return trend;
	}

	/**
	 * Gets the origin indicator
	 * 
	 * @return
	 */
	public AttributeOrigin getOrigin(){
		return origin;
	}

	/**
	 * Sets the origin indicator
	 * 
	 * @param newOrigin
	 */
	public void setOrigin(AttributeOrigin newOrigin){
		origin = newOrigin;
	}

	/**
	 * Serialize the attribute to json.
	 * 
	 * @return json representation.
	 */
	public String toJson() {
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return json;
	}
	
	/**
	 * Implements the equal method between this attribute and other attribute given as parameter.
	 * 
	 * Two attributes are equal if:
	 * 	- Have the same name
	 *  - Have the same type
	 *  - Have the same unit
	 *  - Have the same trend indicator
	 *  - Have the same origin
	 * 
	 * @param other  other attribute to compare
	 * 
	 * @return true if both attributes are equal, false otherwise.
	 */
	public boolean equals(Object o){
		
		if (o instanceof Attribute) {
			Attribute other = (Attribute) o; 
			if ( this.name.compareTo(other.name) != 0 )
				return false;
			
			if (!(this.type.equals(other.type)))
			    return false;
		
			if (!(this.unit.equals(other.unit)))
				return false;
			
			if (this.trend != other.trend)
				return false;
			
			if (this.origin != other.origin)
				return false;
			
			return true;
			
		} else {
			return false;
		}
	}
	
	/** 
	 * Builds a sring representation of the object.
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("name: ").append(name).append(",");
		sb.append("type: ").append(type.toString()).append(",");
		
		if (unit != null)
			sb.append("unit: ").append(unit.toString()).append(",");
		
		sb.append("trend: ").append(trend).append(",");
		sb.append("origin: ").append(origin.toString());
		return sb.toString();
	}

	/**
	 * Updates the attribute data from the information in another attribute. 
	 * @param attribute  attribute with the new information.
	 */
	public void update(Attribute attribute) {
		this.trend = attribute.getTrend();
	}

}
