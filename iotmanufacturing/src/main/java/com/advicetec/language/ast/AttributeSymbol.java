package com.advicetec.language.ast;

/**
 * Represents an attribute in symbol table.
 *  
 * @author Andres Marentes
 */
public class AttributeSymbol extends Symbol 
{
	/**
	 * Unit of measure assigned to the attribute symbol.
	 */
	String unitOfMeasure =null;
	
	/**
	 * We define the attributes marked as trend as those that will be displayed in
	 * the monitoring web application.   
	 */
	boolean trend=false; 
	
	/**
	 * Creates an attribute symbol from its name, type and trend definition. 
	 * 
	 * @param name   Name to be assigned to the attribute
	 * @param type	 Attribute type
	 * @param trend	 Trend definition
	 */
	public AttributeSymbol(String name, Type type, boolean trend) 
	{ 
		super(name,type); 
		this.trend = trend;
	}

	/**
	 * Sets the trend definition.
	 * 
	 * @param trend  trend parameter
	 */
	public void setTrend(boolean trend){
		this.trend = trend;
	}
	
	/**
	 * Sets the unit of measure for the attribute
	 * 
	 * @param unit unit of measure 
	 */
	public void setUnitOfMeasure(String unit)
	{
		this.unitOfMeasure = unit;
	}
	
	/**
	 * Gets the unit of measure of the attribute
	 * 
	 * @return  attribute's unit of measure
	 */
	public String getUnitOfMeasure() {
		return this.unitOfMeasure;
	}
	
	/**
	 * Gets the trend parameter 
	 * 
	 * @return trend parameter 
	 */
	public boolean getTrend(){
		return this.trend;
	}
}
