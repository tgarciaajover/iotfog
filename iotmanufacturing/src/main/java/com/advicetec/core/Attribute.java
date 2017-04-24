package com.advicetec.core;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Describes the attribute.
 * @author user
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
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

	@XmlAttribute
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
	
	@XmlAttribute
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
