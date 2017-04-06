package com.advicetec.core;

public class AttributeValue 
{
	
	String key;
	Attribute type;
	Object value;
	
	// informative members.
	/**
	 * Describes the origin measured entity, job, machine, etc.
	 */
	String parent;
	MeasuredEntityType parentType;
	
	
	public AttributeValue(String key, Attribute type, Object value, String parent, MeasuredEntityType parentType) {
		super();
		
		this.key =  key;
		this.type = type;
		this.value = value;
		this.parent = parent;
		this.parentType = parentType;
	}


	public String getKey() {
		return key;
	}
	
	public Attribute getAttribute()
	{
		return type;
	}


	public String getParent() {
		return parent;
	}


	public MeasuredEntityType getParentType() {
		return parentType;
	}


	public Object getValue() {
		return value;
	}
	
}
