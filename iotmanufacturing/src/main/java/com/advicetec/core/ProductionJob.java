package com.advicetec.core;

import java.util.HashMap;
import java.util.Map;

public class ProductionJob 
{

	String id;
	MeasuredEntityType type;
	ProductionEntity planned;
	ProductionEntity executed;
	Map<String, AttributeValue> expected;
	Map<String, MeasuredAttributeValue> measures;

	public ProductionJob(String id, ProductionEntity planned, ProductionEntity executed) {
		super();
		this.id = id;
		type = MeasuredEntityType.JOB;
		this.planned = planned;
		this.executed = executed;
		this.expected = new HashMap<String, AttributeValue>();
		this.measures = new HashMap<String, MeasuredAttributeValue>();
	}

	public ProductionJob() {
		super();
		this.planned = null;
		this.executed = null;
		this.expected = new HashMap<String, AttributeValue>();
		this.measures = new HashMap<String, MeasuredAttributeValue>();
	}
	
	public String getId() {
		return id;
	}

	public MeasuredEntityType getType() {
		return type;
	}

	public Map<String, AttributeValue> getExpected() {
		return expected;
	}

	public Map<String, MeasuredAttributeValue> getMeasures() {
		return measures;
	}

	public void setPlanned(ProductionEntity planned) {
		this.planned = planned;
	}

	public void setExecuted(ProductionEntity executed) {
		this.executed = executed;
	}

	public ProductionEntity getPlanned() {
		return planned;
	}

	public ProductionEntity getExecuted() {
		return executed;
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
