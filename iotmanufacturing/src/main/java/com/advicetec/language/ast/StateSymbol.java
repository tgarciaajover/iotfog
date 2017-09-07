package com.advicetec.language.ast;

/** 
 * Represents the measured entity state definition in symbol table.
 * It is defined in the global context.
 * 
 * @author Andres Marentes
 */
public class StateSymbol extends Symbol 
{
	/**
	 * Constructor for the state symbol. It is defined as of integer type.
	 */
	public StateSymbol() 
	{ 
		super("state", Type.tINT); 
	}
}
