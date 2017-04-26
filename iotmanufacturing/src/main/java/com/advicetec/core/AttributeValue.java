package com.advicetec.core;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import com.advicetec.measuredentitity.MeasuredEntityType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class models the attribute values from People.
 * @author user
 *
 */
public class AttributeValue 
{

	
	protected String key;
	
	protected Attribute type;
	
	protected Object value;

	// informative members.
	/**
	 * Describes the origin measured entity, job, machine, etc.
	 */
	
	protected String generator;
	
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
			@JsonProperty("type")Attribute type, 
			@JsonProperty("value")Object value, 
			@JsonProperty("generator")String parent, 
			@JsonProperty("generatorType")MeasuredEntityType parentType) {
		super();
		this.key =  key;
		this.type = type;
		this.value = value;
		this.generator = parent;
		this.generatorType = parentType;
	}


	public String getKey() {
		return key;
	}

	public Attribute getType()
	{
		return type;
	}


	public String getGenerator() {
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
		sb.append("type: ").append(type.toString()).append(",");
		sb.append("value: ").append(value.toString()).append(",");
		sb.append("generator: ").append(generator).append(",");
		sb.append("generator type: ").append(generatorType.toString());
		return sb.toString();
	}


	public String toJson() {
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			System.err.println("Cannot export as Attribute Value as the json object.");
			e.printStackTrace();
		}
		return json;
	}

}
