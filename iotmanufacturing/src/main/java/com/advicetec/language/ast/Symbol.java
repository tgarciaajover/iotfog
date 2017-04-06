package com.advicetec.language.ast;

/**
 * 
 * @author andres
 *
 */
public class Symbol
{

	public static enum Type {tINVALID, tVOID, tINT, tFLOAT, tSTRING, tBOOL, tDATETIME, tDATE, tTIME}
	
	String name; // All symbols at least have a name 
	Type type; // Symbols have types 
	public Scope scope;      // All symbols know what scope contains them.

	public Symbol(String name) 
	{ 
		this.name = name;
	} 
	
	public Symbol(String name, Type type) 
	{
		this(name); 
		this.type = type;
	} 
	
	public String getName() 
	{ 
		return name; 
	} 
	
	public Type getType()
	{
		return this.type;
	}
	
	public String toString() 
	{ 
		if ( type!=null ) return '<' +getName()+ ":" + type + '>' ; 
		return getName(); 
	}

    public static String stripBrackets(String s) {
        return s.substring(1,s.length()-1);
    }
	
}
