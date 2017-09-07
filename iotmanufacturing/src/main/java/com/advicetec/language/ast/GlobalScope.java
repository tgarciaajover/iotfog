package com.advicetec.language.ast;

/**
 * This represents global variables, attributes, and other symbols that should be returned to
 * calling parent
 * 
 * @author Andres Marentes
 *
 */
public class GlobalScope extends BaseScope 
{
	/**
	 * Constructor for the Global scope, this scope has not parent, so it should be null. 
	 */
	public GlobalScope() 
	{ 
		super(null); 
	}
	
	/**
	 * Gets the name of the scope.
	 * 
	 * @return scope's name.
	 */
	public String getScopeName() 
	{ 
		return "global"; 
	}
	
}
