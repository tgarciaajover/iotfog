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
	
	private boolean trend;
	
	private AttributeOrigin origin;
	
	public Attribute(String name, AttributeType type,  MeasuringUnit unit) 
	{
		this.name = name;
		this.type = type;
		this.unit = unit;
		this.trend = false;
		this.origin = AttributeOrigin.BEHAVIOR;
	}
	
	
	public Attribute(String name, AttributeType type, MeasuringUnit unit,
			boolean trend, AttributeOrigin origin) {
		super();
		this.name = name;
		this.type = type;
		this.unit = unit;
		this.trend = trend;
		this.origin = origin;
	}


	public void setTrend(boolean newTrend){
		trend = newTrend;
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
	
	public boolean getTrend(){
		return trend;
	}
	
	public AttributeOrigin getOrigin(){
		return origin;
	}
	
	public void setOrigin(AttributeOrigin newOrigin){
		origin = newOrigin;
	}
}
