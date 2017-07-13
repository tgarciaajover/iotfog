package com.advicetec.measuredentitity;

import java.util.HashMap;
import java.util.Map;

import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.MeasuringUnit;

/**
 * This class models the JobID.
 */
public abstract class ProductionEntity extends MeasuredEntity 
{
	protected int producedUnits;
	protected int defectiveUnits;
	
	protected Map<String, AttributeValue> expected;
	
	public ProductionEntity(Integer id, MeasuredEntityType type) 
	{
		super(id, type);
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
	
	public String getCanonicalIdentifier() {
		return null;
	}
	
}
