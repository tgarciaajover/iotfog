package com.advicetec.FogClasses;

import java.util.HashMap;
import java.util.Map;

/**
 * This class models the JobID.
 */
public class ProductionEntity extends MeasuredEntity 
{
	Map<String, AttributeValue> expected;
	
	public ProductionEntity(String id, MeasuredEntityType type) 
	{
		super(id, type);
		this.type = type;
		this.expected = new HashMap<String, AttributeValue>();
	}
	
	
	public void registerExpectedAttribute(Attribute attribute, Object value)
	{
		AttributeValue val= new AttributeValue(attribute.getName(), attribute, value, getId(), getType()); 
		this.expected.put(val.getKey(), val);
	}
	
	public AttributeValue getExpectedAttribute(Attribute attribute)
	{
		return this.expected.get(attribute.getName());
	}
		
}
