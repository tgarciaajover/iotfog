package com.advicetec.core;

import com.advicetec.measuredentitity.MeasuredEntityType;

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
	public AttributeValue(String key, Attribute type, Object value, String parent, MeasuredEntityType parentType) {
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
	
}
