package com.advicetec.language.ast;

/** Represents a field or parameter definition in symbol table.
 *  Locals and globals aren't declared statically; we create
 *  at runtime; don't bother creating symtab entries for them
 *  after AST creation pass.
 */
public class UnitMeasureSymbol extends Symbol 
{
	private String description;
	
	public UnitMeasureSymbol(String name, String descr) 
	{ 
		super(name,Symbol.Type.tVOID);
		this.description = descr;
	}
	
	public String getDescription(){
		return description;
	}
		
}
