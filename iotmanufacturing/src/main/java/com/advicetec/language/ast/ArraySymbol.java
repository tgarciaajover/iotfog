package com.advicetec.language.ast;

/** 
 * Represents an array definition in symbol table.
 *
 * @author Andres Marentes
 */
public class ArraySymbol extends Symbol 
{
	
	/**
	 * Number of elements to create in the array  
	 */
	int numElements;
	
	/**
	 * Constructor for the class, creates an array symbol from the name, type and number of elements.
	 * 
	 * @param name			Name of the attribute array to create 
	 * @param type			Type of the elements in the attribute array
	 * @param numElements	array's number of elements.
	 */
	public ArraySymbol(String name, Type type, int numElements) 
	{ 
		super(name,type); 
		this.numElements = numElements;
	}
	
	/**
	 * Gets the number of elements in the array.
	 * 
	 * @return numerb of elements.
	 */
	public int getNumElements()
	{
		return numElements;
	}
}
