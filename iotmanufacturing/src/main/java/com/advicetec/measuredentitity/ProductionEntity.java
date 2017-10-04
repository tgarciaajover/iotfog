package com.advicetec.measuredentitity;

import java.util.HashMap;
import java.util.Map;

import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeValue;

/**
 * This class models the JobID.
 */
public abstract class ProductionEntity extends MeasuredEntity 
{
	/**
	 * total produced units 
	 */
	protected int producedUnits;
	
	/**
	 * total defective units 
	 */
	protected int defectiveUnits;
	
	/**
	 * attribute values defined for the production entity. 
	 */
	protected Map<String, AttributeValue> expected;
	
	/**
	 * Constructor for the class 
	 * 
	 * @param id	identifier
	 * @param type	measured entity type
	 */
	public ProductionEntity(Integer id, MeasuredEntityType type) 
	{
		super(id, type);
		this.expected = new HashMap<String, AttributeValue>();
	}
	
	
	/**
	 * Registers an attribute value in the production entity.
	 *  
	 * @param attribute  attribute to register
	 * @param value		 value of the attribute.
	 */
	public void registerExpectedAttribute(Attribute attribute, Object value)
	{
		AttributeValue val= new AttributeValue(attribute.getName(), attribute, value, getId(), getType()); 
		this.expected.put(val.getKey(), val);
	}
	
	/**
	 * Gets an attribute value from the production entity
	 *  
	 * @param attribute		attribute for which we want to get its value
	 * @return	attribute value.
	 */
	public AttributeValue getExpectedAttribute(Attribute attribute)
	{
		return this.expected.get(attribute.getName());
	}
	
	/**
	 * Gets the canonical identifier of the production entity
	 */
	public String getCanonicalIdentifier() {
		return null;
	}
	
}
