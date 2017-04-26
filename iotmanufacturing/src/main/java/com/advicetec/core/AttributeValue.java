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
	protected MeasuredEntityType parentType;

	/**
	 * 
	 * @param key Identifier
	 * @param type Type
	 * @param value Value
	 * @param parent Describes the origin of the measuring entity. e.g. machine, jobid
	 * @param parentType Type of parent.
	 */
	@JsonCreator
	public AttributeValue(@JsonProperty("key")String key, @JsonProperty("type")Attribute type, 
			@JsonProperty("value")Object value, @JsonProperty("generator")String parent, 
			@JsonProperty("parentType")MeasuredEntityType parentType) {
		super();

		this.key =  key;
		this.type = type;
		this.value = value;
		this.generator = parent;
		this.parentType = parentType;
	}


	public String getKey() {
		return key;
	}

	public Attribute getAttribute()
	{
		return type;
	}


	public String getGenerator() {
		return generator;
	}


	public MeasuredEntityType getGeneratorType() {
		return parentType;
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
		sb.append("parent type: ").append(parentType.toString());
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
