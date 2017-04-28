package com.advicetec.core;

import java.io.IOException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.core.serialization.AttributeOriginSerializer;
import com.advicetec.core.serialization.AttributeOriginDeserializer;
import com.advicetec.core.serialization.AttributeTypeDeserializer;
import com.advicetec.core.serialization.AttributeTypeSerializer;
import com.advicetec.core.serialization.MeasuringUnitSerializer;
import com.advicetec.core.serialization.MeasuringUnitDeserializer;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


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
	@JsonSerialize(using = AttributeTypeSerializer.class)
	@JsonDeserialize(using = AttributeTypeDeserializer.class)
	private AttributeType type;
	// non mandatory
	@JsonSerialize(using = MeasuringUnitSerializer.class)
	@JsonDeserialize(using = MeasuringUnitDeserializer.class)
	private MeasuringUnit unit;
	private boolean trend;

	@JsonSerialize(using = AttributeOriginSerializer.class)
	@JsonDeserialize(using = AttributeOriginDeserializer.class)
	private AttributeOrigin origin;

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

	public Attribute(String name, AttributeType type,  MeasuringUnit unit) 
	{
		this.name = name;
		this.type = type;
		this.unit = unit;
		this.trend = false;
		this.origin = AttributeOrigin.BEHAVIOR;
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
		} catch (IOException e) {
			System.err.println("Cannot export this Attribute as the json object.");
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

	public void update(Attribute attribute) {
		this.trend = attribute.getTrend();
	}

}
