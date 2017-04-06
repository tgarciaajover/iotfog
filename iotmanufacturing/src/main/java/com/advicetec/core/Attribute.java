package com.advicetec.core;

/**
 * Describes the attribute.
 * @author user
 *
 */
public class Attribute 
{
	String name;
	AttributeType type;	
	MeasuringUnit unit;

	public Attribute(String name, AttributeType type, MeasuringUnit unit) 
	{
		super();
		this.name = name;
		this.type = type;
		this.unit = unit;
	}

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
	
	
}
