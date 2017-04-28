package com.advicetec.core;

import java.io.IOException;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.ObjectMapper;

import com.advicetec.measuredentitity.MeasuredAttributeValue;
import com.advicetec.measuredentitity.MeasuredEntityType;



/**
 * This class models the attribute values from People.
 * @author user
 *
 */
@JsonTypeInfo(
		use=JsonTypeInfo.Id.NAME,
		include=JsonTypeInfo.As.PROPERTY,
	property="sub")
@JsonSubTypes({
	@Type(value=MeasuredAttributeValue.class,name="MeasuredAttributeValue")
})
public class AttributeValue
{

	
	protected String key;
	
	protected Attribute attr;
	
	protected Object value;

	// informative members.
	/**
	 * Describes the origin measured entity, job, machine, etc.
	 */
	
	protected Integer generator;
	
	protected MeasuredEntityType generatorType;

	/**
	 * 
	 * @param key Identifier
	 * @param type Type
	 * @param value Value
	 * @param parent Describes the origin of the measuring entity. e.g. machine, jobid
	 * @param parentType Type of parent.
	 */
	@JsonCreator
	public AttributeValue(@JsonProperty("key")String key, 
			@JsonProperty("attr")Attribute attr, 
			@JsonProperty("value")Object value, 
			@JsonProperty("generator")Integer parent, 
			@JsonProperty("generatorType")MeasuredEntityType parentType) {
		super();
		this.key =  key;
		this.attr = attr;
		this.value = value;
		this.generator = parent;
		this.generatorType = parentType;
	}


	public String getKey() {
		return key;
	}
	
	public Attribute getAttr()
	{
		return attr;
	}


	public Integer getGenerator() {
		return generator;
	}


	public MeasuredEntityType getGeneratorType() {
		return generatorType;
	}


	public Object getValue() {
		return value;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("key: ").append(key).append(",");
		sb.append("attr: ").append(attr.toString()).append(",");
		sb.append("value: ").append(value.toString()).append(",");
		sb.append("generator: ").append(generator).append(",");
		sb.append("generator type: ").append(generatorType.toString());
		return sb.toString();
	}


	public String toJson() {
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			System.err.println("Cannot export as Attribute Value as the json object.");
			e.printStackTrace();
		}
		return json;
	}

}
