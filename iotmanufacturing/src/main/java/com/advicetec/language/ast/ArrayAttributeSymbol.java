package com.advicetec.language.ast;

/** 
 * Represents an array of attributes in symbol table.
 * 
 * @author Andres Marentes
 */
public class ArrayAttributeSymbol extends ArraySymbol 
{
	String unitOfMeasure =null;
	
	/**
	 * Constructor for the class, creates an attribute array symbol from the name, its type and number of elements.
	 * 
	 * @param name			Name of the attribute array to create 
	 * @param type			Type of the elements in the attribute array 
	 * @param numElements   array's number of elements.
	 */
	public ArrayAttributeSymbol(String name, Type type, int numElements) 
	{ 
		super(name,type, numElements); 
	}

	/**
	 * Sets the unit of measure for the array 
	 *  
	 * @param unit  unit of measure symbol name
	 */
	public void setUnitOfMeasure(String unit)
	{
		this.unitOfMeasure = unit;
	}
	
	/**
	 * Gets the unit of measure assigned to the array
	 * 
	 * @return  unit of measure.
	 */
	public String getUnitOfMeasure() {
		return this.unitOfMeasure;
	}
}
