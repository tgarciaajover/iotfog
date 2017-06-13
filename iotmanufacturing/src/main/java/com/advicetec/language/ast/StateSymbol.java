package com.advicetec.language.ast;

/** Represents the measured entity state definition in symbol table.
 *  it is defined in the global context.
 */
public class StateSymbol extends Symbol 
{
	public StateSymbol() 
	{ 
		super("state", Type.tINT); 
	}
}
