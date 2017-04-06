package com.advicetec.language.ast;

/** Represents a field or parameter definition in symbol table.
 *  Locals and globals aren't declared statically; we create
 *  at runtime; don't bother creating symtab entries for them
 *  after AST creation pass.
 */
public class ArraySymbol extends Symbol 
{
	
	int numElements;
	
	public ArraySymbol(String name, Type type, int numElements) 
	{ 
		super(name,type); 
		this.numElements = numElements;
	}
	
	public int getNumElements()
	{
		return numElements;
	}
}
