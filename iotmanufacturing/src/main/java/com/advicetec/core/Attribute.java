package com.advicetec.core;

/**
 * Describes the attribute.
 * @author user
 *
 */
public class Attribute 
{
	private String name;
	private AttributeType type;
	// non mandatory
	private MeasuringUnit unit;
	
	
	public Attribute(String name, AttributeType type, MeasuringUnit unit) 
	{
		this.name = name;
		this.type = type;
		this.unit = unit;
	}
	
	
	public Attribute(String name, AttributeType type){
		this(name,type,null);
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
