package com.advicetec.language.ast;

/** 
 * Represents a variable definition in symbol table.
 * 
 * In the language variables are temporal memory that helps to calculate attributes.
 * Because of their temporal nature, they are deleted at then end of the 
 * behavior or transformation execution.  
 */
public class VariableSymbol extends Symbol 
{
	/**
	 * Constructor for the class, it takes:
	 * 
	 * @param name  name of the variable
	 * @param type	type of the variable.
	 */
	public VariableSymbol(String name, Type type) 
	{ 
		super(name,type); 
	}
}
