package com.advicetec.language.ast;

/** 
 * Represents the unit of measure to be used by a variable or attribute defined in 
 * the symbol table.
 */
public class UnitMeasureSymbol extends Symbol 
{
	
	/**
	 *  unit of measure description  
	 */
	private String description;
	
	/**
	 * Constructor for the class, it takes:
	 * @param name  name of the unit of measure
	 * @param descr	description of the unit of measure
	 */
	public UnitMeasureSymbol(String name, String descr) 
	{ 
		super(name,Symbol.Type.tVOID);
		this.description = descr;
	}
	
	/**
	 * Gets the description of the unit of measure
	 * 
	 * @return Unit of measure description.
	 */
	public String getDescription(){
		return description;
	}
		
}
