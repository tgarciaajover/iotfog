package com.advicetec.language.ast;

/** 
 * Represents a special symbol for built in types such int, float primitive types 
 * 
 * @author Andres Marentes
 */
public class BuiltInTypeSymbol extends Symbol implements Type 
{ 
	/**
	 * Builds the symbol for primitive types.
	 *   
	 * @param name name of the primitive type.
	 */
	public BuiltInTypeSymbol(String name) 
	{ 
		super(name); 
	} 

}
