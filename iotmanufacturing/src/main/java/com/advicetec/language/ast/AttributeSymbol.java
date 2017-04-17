package com.advicetec.language.ast;

/** Represents a field or parameter definition in symbol table.
 *  Locals and globals aren't declared statically; we create
 *  at runtime; don't bother creating symtab entries for them
 *  after AST creation pass.
 */
public class AttributeSymbol extends Symbol 
{
	String unitOfMeasure =null;
	boolean trend=false; 
	
	public AttributeSymbol(String name, Type type, boolean trend) 
	{ 
		super(name,type); 
		this.trend = trend;
	}

	public void setTrend(boolean trend){
		this.trend = trend;
	}
	
	public void setUnitOfMeasure(String unit)
	{
		this.unitOfMeasure = unit;
	}
	
	public String getUnitOfMeasure() {
		return this.unitOfMeasure;
	}
	
	public boolean getTrend(){
		return this.trend;
	}
}
