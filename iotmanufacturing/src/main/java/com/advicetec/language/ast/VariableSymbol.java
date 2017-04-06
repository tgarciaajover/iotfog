package com.advicetec.language.ast;

/** Represents a field or parameter definition in symbol table.
 *  Locals and globals aren't declared statically; we create
 *  at runtime; don't bother creating symtab entries for them
 *  after AST creation pass.
 */
public class VariableSymbol extends Symbol 
{
	public VariableSymbol(String name, Type type) 
	{ 
		super(name,type); 
	}
}
