package com.advicetec.core;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.core.serialization.AttributeOriginSerializer;
import com.advicetec.core.serialization.MeasuringUnitSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Describes the attribute.
 * @author user
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Attribute 
{
	private String name;
	private AttributeType type;
	// non mandatory
	@JsonSerialize(using = MeasuringUnitSerializer.class)
	private MeasuringUnit unit;
	private boolean trend;

	@JsonSerialize(using = AttributeOriginSerializer.class)
	private AttributeOrigin origin;

	public Attribute(String name, AttributeType type,  MeasuringUnit unit) 
	{
		this.name = name;
		this.type = type;
		this.unit = unit;
		this.trend = false;
		this.origin = AttributeOrigin.BEHAVIOR;
	}

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


	public void setTrend(boolean newTrend){
		trend = newTrend;
	}

	public Attribute(String name, AttributeType type){
		this(name,type,null);
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public MeasuringUnit getUnit() {
		return unit;
	}

	public AttributeType getType()
	{
		return this.type;
	}

	@XmlAttribute
	public boolean getTrend(){
		return trend;
	}

	public AttributeOrigin getOrigin(){
		return origin;
	}

	public void setOrigin(AttributeOrigin newOrigin){
		origin = newOrigin;
	}


	public String toJson() {
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			System.err.println("Cannot export as Attribute as the json object.");
			e.printStackTrace();
		}
		return json;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("name: ").append(name).append(",");
		sb.append("type: ").append(type.toString()).append(",");
		sb.append("unit: ").append(unit.toString()).append(",");
		sb.append("trend: ").append(trend).append(",");
		sb.append("origin: ").append(origin.toString());
		return sb.toString();
	}
}
